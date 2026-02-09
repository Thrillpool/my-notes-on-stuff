## What really is kubernetes, what's it got that docker ain't got?

It's basically like very fancy way of managing containers, across multiple different nodes.

Some key points, all the immediate juicy stuff is in kube-system namespace.

1. API server, a server which allows one to interact with the cluster. It itself is run in kubernetes
under the kube-apiserver pods.
2. etcd, to store all the relevant data for the cluster.
3. kube-scheduler, works out how to actually instantiate your workloads.
4. kube-controller, the thing that actually creates stuff based on the desired state.
5. some form of dns so that everything can talk to everything
6. Each node has a 'kubelet' on it, which is the thing the controller talks to and manages the actual creation on that node.

Summarising, the actual work happens on 'nodes', no surprises there, the nodes have a special program on them 'kubelet' which is
how things are orchestrated. Naturally there is some container runtime also. The api server takes in requests and effectively changes the
desired state (as well as exposing info about the cluster). The controller checks this new desired state and worked out how to make
it so, the scheduler decides how to put desired work onto nodes.

Making these nodes concrete, `minikube ssh` -> `docker ps`

## The DNS, for minikube

```sh
thrillpool@thrillpool:~$ kubectl -n kube-system get svc
NAME       TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)                  AGE
kube-dns   ClusterIP   10.96.0.10   <none>        53/UDP,53/TCP,9153/TCP   27m
```

That 10.96.0.10 will then be what other pods configure as their dns.

e.g. do `kubectl apply -f deployments/curl-client.yml`, then exec into the created pod and

```sh
~ $ cat /etc/resolv.conf 
nameserver 10.96.0.10
search default.svc.cluster.local svc.cluster.local cluster.local home
options ndots:5
```

## What's in the exciting etcd?

Do `kubectl apply -f pods/etcdctl.yml` and `kubectl logs etcdctl` to see (spoilers, it's everything! Everything you want to make as state in etcd and things read etcd to work out the desired state etc.)

## Basics of api server

In general, kubectl is just making lots of calls to a rest api. Can see these calls by specifying a high level of verbosity in kubectl calls, e.g.
`kubectl get po -v=8`


How to authenticate - check your `~/.kube/config`. Can curl the kube api (or rather start to try) like
`curl --cacert /home/thrillpool/.minikube/ca.crt https://192.168.49.2:8443` for instance.

To send your creds, in the case of minikube it uses client certificates
https://kubernetes.io/docs/setup/best-practices/certificates/#kubelet-s-server-and-client-certificates

`curl --cacert /home/thrillpool/.minikube/ca.crt --cert /home/thrillpool/.minikube/profiles/minikube/client.crt --key /home/thrillpool/.minikube/profiles/minikube/client.key https://192.168.49.2:8443`.

That's great, although it's not how we do it, we do it in the more well known (to me at least) way, a token!

In general see https://kubernetes.io/docs/reference/access-authn-authz/authentication/#authentication-methods for list of methods.

## Rest
Now see kube-behaviour-testing/keda/http-server-example