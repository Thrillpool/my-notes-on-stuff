## What is Netty?

It's a framework for building servers in Java (at a very low level). Lots of more abstract libraries are backed by it so it makes sense to know about. Side note, their versions all end .Final which I've always found so funny that I've never bothered to understand what they mean.

## Where are the official docs?
They're here https://netty.io/wiki/user-guide-for-4.x.html but there's a wealth of examples here https://github.com/netty/netty/tree/4.1/example/src/main/java/io/netty/example as well as javadocs here https://netty.io/4.2/api/index.html.

## How to use it?

If you've seen a server before, it's all pretty intuitive so I just present a basic example (taken from https://www.baeldung.com/netty) for you to read and infer from.

Since the above page doesn't detail how to actually call the server, and as that's the most interesting bit and a reminder that really netty is low level (or more accurately, exposes low level stuff), here we operate at level of just tcp connection and so we have to write the bytes directly, you can do

```
thrillpool@DESKTOP:~$ echo -e '\x00\x00\x00\x04\x00\x00\x00\x05\x48\x65\x6c\x6c\x6f\x0a' | netcat  192.168.56.1 8080 > ~/responsebytes
thrillpool@DESKTOP:~$ xxd responsebytes
00000000: 0000 0008
```

i.e. the server 'correctly' sent back int 00 00 00 08, twice of int 00 00 00 04 as per the code.