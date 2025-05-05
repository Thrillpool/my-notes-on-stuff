## What is Envoy

In short Envoy is a heavily customisable proxy. The context in which I was initially made aware of it is when used as a sidecar to every service. That is to say, you never talk to services directly any more, instead all communication happens via a dedicated envoy instance for the service, there's a huge number of benefits to this, just as a for instance, you don't have to have you service understand tls, envoy can deal with the tls and forward the unencrypted traffic to your service.

## Where are the official docs?
They are e.g. here https://www.envoyproxy.io/docs/envoy/v1.34.0/, they are good, having gone through the little example I present here you're well equipped to make use of them.

## Why do I want to know about Envoy

Well, istio uses it, really the majority of the complexity of istio is actually complexity of envoy (and istio just provides you mechanisms of configuring envoy). Working with envoy directly for a bit gives you one less layer to get confused by. In fact! The istio docs can be a bit underwhelming at times, and it's really just that they are rightfully delegating to envoy docs.

## Getting to grips with some basics

There's a huge amount (too much) to know about Envoy. Luckily the docs do exist and are good, we just need a little help to get started understanding hwo to read them.

First of all, actually running envoy, we'll use docker, the image is e.g. envoyproxy/envoy:v1.34.0. Configuration of envoy is largely done via yaml. Let's run envoy with the envoy-demo.yml file in this repo, experimentally see what it does, understand each line of configuration and understand how we could understand these things!

To run it, do (from this directory)

```bash
docker run --rm -it \
      -v $(pwd)/envoy-demo.yml:/envoy-demo.yml \
      -p 9901:9901 \
      -p 10000:10000 \
      envoyproxy/envoy:v1.34.0 \
          -c /envoy-demo.yml
```

Right let's understand this yaml.

The top level thing is this `static_resources`, this is the static configuration for envoy. The schema is [here](https://www.envoyproxy.io/docs/envoy/v1.34.0/api-v3/config/bootstrap/v3/bootstrap.proto#envoy-v3-api-msg-config-bootstrap-v3-bootstrap-staticresources).

Listeners is a sequence of [Listener](https://www.envoyproxy.io/docs/envoy/v1.34.0/api-v3/config/listener/v3/listener.proto#envoy-v3-api-msg-config-listener-v3-listener), makes sense! It binds to some interface (or rather in our case 0.0.0.0 so all network interfaces) and to some port.

It defines a [filter chain](https://www.envoyproxy.io/docs/envoy/v1.34.0/api-v3/config/listener/v3/listener_components.proto#envoy-v3-api-msg-config-listener-v3-filterchain) which is the mechanism by which you define how requests are actually handled. In our case we just have the one filter. The typed_config is the interesting bit, it has... no type really. To actually fine what you can put in here you can look around here https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/filter/network/network. In this case we have HTTP connection manager as evidenced by the @type linking to type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager which is defined [here](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/http_connection_manager/v3/http_connection_manager.proto#envoy-v3-api-msg-extensions-filters-network-http-connection-manager-v3-httpconnectionmanager).

Lots of the configuration after that is self evident (and in any case documented in the above). But let's call out some interesting stuff.

`access_log` is a good one, the http access logs are extremely useful, our config uses [this type](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/access_loggers/stream/v3/stream.proto#envoy-v3-api-msg-extensions-access-loggers-stream-v3-stdoutaccesslog). We don't change the log_format, so the default is used, which actually is very very well worth getting to grips with as often the default is what is in use, and there's no headers or anything so you just need to know what things mean. The default format is defined here https://www.envoyproxy.io/docs/envoy/latest/configuration/observability/access_log/usage#config-access-log-default-format.

We haven't spoken about actually contacting our running envoy yet, so let's do that.

Doing 

```bash
curl -v localhost:10000/hi 
```
    
We see some response, whatever, if we look at the logs envoy side we see a log like so

```
"GET /hi HTTP/1.1" 404 - 0 398 215 214 "-" "curl/8.5.0" "37b74b15-0b9f-4ecd-ad7f-d3ac1fd2c24a" "www.envoyproxy.io" "3.124.100.143:443"
```

We have the request method, request path, protocol, response code, response flags (here a -, the response flags are things that normally tell you error stuff, e.g. you might see 'DC' downstream connection termination), bytes received (0 as we sent no body, try sending a body and you will see it be the appropraite numer), bytes sent (i.e. the size of the response), duration (milliseconds from start time to final byte written), response time set in x-envoy-upstream-service-time header (strange), x-forwarded-for header, user-agent header (curl), x-request-id header, authority header (essentially who the request went to), ip port of upstream host.

In general it's easy to capture lots of other useful things, e.g. arbitrary headers and so on.

We define a http_filter of type type.googleapis.com/envoy.extensions.filters.http.router.v3.Router, really all we are doing is enabling it, it's in a sense the trivial filter that says to actually send off the thing to the destination. How things are actually routed isn't defined here.

Instead we have a route_config, it's intuitive enough, the [virtualhost](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/route/v3/route_components.proto#envoy-v3-api-msg-config-route-v3-virtualhost) is the interesting bit those with some understanding of istio but not envoy will appreciate that virtualhost bears a lot of resembalance to virtual service in istio. It matches all domains and any request prefixed with '/' (i.e. all requests), and rewrites the host to be www.envoyproxy.io. This cluster is an interesting bit, we can see it refers to a cluster we define in our clusters configuration so onto that!

A cluster is a grouping of upstreams that envoy is allowed to send traffic to. There's a huge amount of fanciness here, e.g. how the dns is defined is exciting https://www.envoyproxy.io/docs/envoy/v1.34.0/intro/arch_overview/upstream/service_discovery#arch-overview-service-discovery-types-logical-dns. We define this transport_socket thing that configures how tls is done. load_assignment in principle lets us define how the loadbalancing is done though as we have only one endpoint it's all a bit redundant.
