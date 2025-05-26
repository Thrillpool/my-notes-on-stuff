#include <dlfcn.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <stdio.h>

int thrillpool_count = 0;

int connect(int sockfd, const struct sockaddr *addr, socklen_t addrlen) {
    static int (*real_connect)(int, const struct sockaddr *, socklen_t) = NULL;
    if (!real_connect) real_connect = dlsym(RTLD_NEXT, "connect");

    if (thrillpool_count == 0) {
        sleep(3);
    }

    thrillpool_count++;

    return real_connect(sockfd, addr, addrlen);
}