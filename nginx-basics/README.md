## What is nginx?

It's a reverse proxy (or typically used as one at least), it can also be used as a web server reasonably.

## What should I know about it

Not much really, it can do some cool stuff of course, but every time I've seen it used it's been a very simple usage. https://nginx.org/en/docs/beginners_guide.html is really enough to get you by, in short you define for some endpoints either to serve some content or forward onto some other endpoint.

In any case just for convenience, provided is a sample nginx configuration you can run

```bash
docker run --rm --name my-custom-nginx-container -v ./nginx.conf:/etc/nginx/nginx.conf:ro -p 8080:8080 -it nginx
```

If you curl localhost:8080, you'll see a response from google.