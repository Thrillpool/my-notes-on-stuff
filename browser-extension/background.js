// This is like if you want to cancel request, which is cool I guess to block ads etc.
// See https://developer.mozilla.org/en-US/docs/Mozilla/Add-ons/WebExtensions/API/webRequest/BlockingResponse
// for all the things can return.
function onBeforeRequestCancelExample(details) {
  return { cancel: true };
}

// This is like redirect one request to something else.
// Redirecting is ultimately the best thing you can do, because you can delegate all the logic to some proxy server
// yay! Of course, the other things are convenient if you don't want to run proxy servers.
function onBeforeRequestRedirectExample(details) {
  return {
    redirectUrl: details.url.replace("www.yahoo.com", "www.google.com"),
  };
}

// One cool thing is that all these methods can be async, i.e. you can do arbitrary networking stuff etc.
// to resolve the response you want
function onBeforeRequestCancelFancyExample(details) {
  return new Promise((resolve, reject) => {
    resolve({ cancel: true });
  });
}

// This is like if you want to modify or add some request headers
function onBeforeSendHeadersExample(details) {
  details.requestHeaders.push({ name: "x-thrill-header", value: "some-value" });
  return {};
}

// This is like if you want to modify or add some response headers
function onHeadersReceivedExample(details) {
    details.responseHeaders.push({ name: "x-thrill-header", value: "some-value" });
    return {};
}

// See https://developer.mozilla.org/en-US/docs/Mozilla/Add-ons/WebExtensions/API/webRequest/filterResponseData
function onBeforeRequestModifyResponse(details) {
  let filter = browser.webRequest.filterResponseData(details.requestId);
  let decoder = new TextDecoder("utf-8");
  let encoder = new TextEncoder();
  filter.ondata = (event) => {
    let str = decoder.decode(event.data, { stream: true });
    str = str.replace("<head>", "<head><style>body { background-color: indianred !important }</style>");
    filter.write(encoder.encode(str));
  };

  filter.onstop = (event) => {
    filter.close();
  };

  return {};
}

browser.webRequest.onBeforeRequest.addListener(
  onBeforeRequestCancelExample,
  { urls: ["https://www.reddit.com/*"] },
  ["blocking"]
);

browser.webRequest.onBeforeRequest.addListener(
  onBeforeRequestCancelFancyExample,
  { urls: ["https://www.youtube.com/*"] },
  ["blocking"]
);

browser.webRequest.onBeforeRequest.addListener(
  onBeforeRequestRedirectExample,
  { urls: ["https://www.yahoo.com/*"] },
  ["blocking"]
);

browser.webRequest.onBeforeRequest.addListener(
  onBeforeRequestRedirectSantaExample,
  { urls: ["data:image*"] },
  ["blocking"]
);

browser.webRequest.onBeforeSendHeaders.addListener(
  onBeforeSendHeadersExample,
  { urls: ["https://developer.mozilla.org/*"] },
  // important to put requestHeaders here to make sure it's passed into the listener
  ["blocking", "requestHeaders"]
);

browser.webRequest.onHeadersReceived.addListener(
  onHeadersReceivedExample,
  { urls: ["https://developer.mozilla.org/*"] },
  ["blocking", "responseHeaders"],
);

browser.webRequest.onBeforeRequest.addListener(
  onBeforeRequestModifyResponse,
  // see https://developer.mozilla.org/en-US/docs/Mozilla/Add-ons/WebExtensions/API/webRequest/ResourceType
  { urls: ["<all_urls>"], types: ["main_frame"]},
  ["blocking"]
);