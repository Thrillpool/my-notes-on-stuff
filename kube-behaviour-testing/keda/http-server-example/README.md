## What is this thing?
This is an artificial way to play around with Keda and see how it works.

It has a server which has an artificial 'memory leak' so that you can make a pod go above 50% threshold memory and so trigger keda auto scale up.

Then when you want to scale it down, it has mechanism to clear out the memory and so trigger keda scale down.

## What do I need to do to use this?
Download minikube https://minikube.sigs.k8s.io/docs/start/?arch=%2Flinux%2Fx86-64%2Fstable%2Fbinary+download, you don't need to install it anywhere you can just run the binary.

Run `minikube start`

Run `minikube addons enable metrics-server` to enable kube metric server

Run `kubectl apply -f keda.yml` to get keda into your cluster

Run 

```bash
eval $(minikube docker-env)
docker build -t node-mem-leak-server .
```

to build the image (in the context of minikube).

Run `kubectl apply -f memory-leak-server.yml` to make the memory leak server deployment, service, scaledobject and also make a curl pod deployment we'll use to interact with the server.


## How do I use it now?

Do `kubectl get po` and get the name of the curl-client pod, then exec into it, e.g. `kubectl exec -it curl-client-787b887579-ntzdj -- sh`

Do `curl node-mem-leak-server-service:8000` a number of times, you should get increasingly larger numbers back until it stabilises, this means that the pod has reached max memory usage it's going to 'leak' 

You may now `exit` and a `kubectl get po` should reveal a new pod being made eventually, `kubectl get hpa` should say something like

```
NAME                                                 REFERENCE                         TARGETS           MINPODS   MAXPODS   REPLICAS   AGE
keda-hpa-node-mem-leak-server-memory-scaled-object   Deployment/node-mem-leak-server   memory: 46%/60%   1         10        2          29m
```

Now go back into the curl pod and curl some more until you get the stable large number, now both pods are at max memory usage.

Exiting again you should see the hpa again over capacity and eventually the new pods made.

You can do like

`kubectl -n keda logs -f keda-operator-64cb45cd8b-4dflj` to get some light logging on what keda is doing.

Rinse and repeat as much as you like (maybe even until you reach the maximum 10 replicas).

When you want to scale down, go into the curl pod again and do `curl node-mem-leak-server-service:8000/clear-me` a number of times to reset the memory leaks in each pod.

Looking at the hpa you should then see something like

```
NAME                                                 REFERENCE                         TARGETS           MINPODS   MAXPODS   REPLICAS   AGE
keda-hpa-node-mem-leak-server-memory-scaled-object   Deployment/node-mem-leak-server   memory: 19%/60%   1         10        3          33m
```

Eventually it will scale down, note that we've set

```
stabilizationWindowSeconds: 10
```

to speed up this process somewhat.

So that the hpa that keda makes looks like so 

```
spec:
  behavior:
    scaleDown:
      policies:
      - periodSeconds: 15
        type: Percent
        value: 100
      selectPolicy: Max
      stabilizationWindowSeconds: 10
    scaleUp:
      policies:
      - periodSeconds: 15
        type: Pods
        value: 4
      - periodSeconds: 15
        type: Percent
        value: 100
      selectPolicy: Max
      stabilizationWindowSeconds: 0
```
