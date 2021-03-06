# projector-client-web
A web client for Projector.

Contents:
* [Running](#running).
* [Building](#building).
* [Notes](#notes) &mdash; tested browsers.
* [Page parameters](#page-parameters) &mdash; set up the connection.
* [Shortcuts](#shortcuts) &mdash; currently they are needed for debug purposes.
* [Known issues](#known-issues) &mdash; the current state of limitations we believe there is no way to handle.

## Running
The latest commit in master is built and deployed to GitHub Pages. So the latest commit is available here: <https://jetbrains.github.io/projector-client/index.html?host=HOST&port=PORT> (don't forget to set `HOST` and `PORT` to match your server's).

The latest version can be downloaded at the [Artifacts page](https://github.com/JetBrains/projector-client/releases).

## Building
The client needs only static files. The following script will build the client files in the `projector-client-web/build/distributions` dir:
```shell script
./gradlew :projector-client-web:browserProductionWebpack
```

After building, you can run the HTML page: `projector-client-web/build/distributions/index.html`.

## Notes
Tested browsers:
- Chromium.
- Firefox.

## Page parameters
You can set some settings in query parameters like this: `index.html?host=localhost&port=8887&wss`. Actual list of parameters can be found in the `ParamsProvider.kt` file. Here we describe them.

### Main parameters
Name | Type | Default value | Description 
---|---|---|---
`host` | String | Host of the web page | Set the host of `projector-server` to connect.
`port` | String | `8887` | Set the port of `projector-server` to connect.
`wss` | Presence | Protocol of the web page | Enable security of WebSocket connection.
`notSecureWarning` | Boolean | `true` | Enable warning saying that the context is not [secure](https://developer.mozilla.org/en-US/docs/Web/Security/Secure_Contexts).
`token` | String? | Not present | Set a password which will be checked by the server on the connection.
`mobile` | String? | Not present | Enable overlay controls handy for mobile devices. Presented param activates all controls. Provide `onlyButtons` value if you don't use virtual keyboard.
`ideWindow` | Int? | Not present | Specify the IDE window ID to show. The first ID is `0`. If not presented, all IDE windows are shown.

### Debug/test parameters
Name | Type | Default value | Description 
---|---|---|---
`clipping` | Presence | Not present | Show borders of clipping areas via red and blue lines.
`logUnsupportedEvents` | Presence | Not present | Log unsupported events received from server to browser console.
`doubleBuffering` | Presence | Not present | Enable double buffering for every single message from server.
`enableCompression` | Presence | Not present | Use compression for sending and receiving WebSocket messages.
`toClientFormat` | String | `jsonManual` | Sets format of data from server to client: `json`, `jsonManual`, `protoBuf`.
`imageTtl` | Double | `60_000.0` | Set caching time of unused images in ms.
`flushDelay` | Int? | `1` | Set buffering time of events from client in ms. If the value is not integer, unbuffered mode is used: every client event is sent to the server immediately.
`showTextWidth` | Presence | Not present | Show near-text lines of browser width and desired width.
`showSentReceived` | Presence | Not present | Show blinking indicators in the corner of the screen when events were sent or received.
`showPing` | Presence | Not present | Show some info of simple ping to and from server.
`pingAverageCount` | Int? | Not present | Activate displaying average ping of this number of iterations.
`backgroundColor` | String | `2A2` (green) | Set color of area where there are no windows.
`userScalingRatio` | Double | `1.0` | Set scaling ratio.
`pingInterval` | Int | `1000` | Set interval of pinging in ms.
`showProcessingTime` | Presence | Not present | Log processing time of server messages to browser console.
`repaintArea` | Presence | Not present | Enable ability to see repainted areas, use a shortcut to toggle (more info below).
`speculativeTyping` | Presence | Not present | Enable rendering symbols in Editor not waiting for draw events from server.
`repaintInterval` | Int | `333` | Set interval of repainting that is needed to paint loaded images in ms.
`cacheSize` | Int | `5M` | Set size of cache for images in Chars.
`blockClosing` | Boolean | `true` | Enable blocking of accidental closing of the web page

## Shortcuts
- `Ctrl + F10` prints statistics to the browser console. Example:  
```
[INFO] :: ClientStats :: Stats:

simple ping average:                    20.84 (19 iterations)
to-client message size average:         3K bytes (185 iterations)
to-client compression ratio average:    1.00 (185 iterations)
draw event count average:               56 (185 iterations)
decompressing time average:             0.00 ms (185 iterations)
decoding time average:                  12.51 ms (185 iterations)
drawing time average:                   3.11 ms (185 iterations)
other processing time average:          0.20 ms (185 iterations)
total (sum) time average:               15.83 ms (185 iterations)

to-client message size rate:    30K bytes per second (21.7 seconds)
draw event count rate:          477 per second (21.7 seconds)
decompressing time rate:        0.04 ms per second (21.7 seconds)
decoding time rate:             106.65 ms per second (21.7 seconds)
drawing time rate:              26.52 ms per second (21.7 seconds)
other processing time rate:     1.72 ms per second (21.7 seconds)
total (sum) time rate:          134.92 ms per second (21.7 seconds)

Stats are reset!
```
- `Ctrl + F11` toggles showing repainted areas (if `repaintArea` query param is enabled).

## Known issues
Due to limitations of web browsers, there are some issues in the Web Client. They can be solved via native implementations of the client.

### Some hotkeys are intercepted by the browser
For example, `Ctrl+Q` in Windows/Linux or `Cmd+N` in Mac is handled by the browser.

Since some shortcuts close the tab or the window, we implemented a confirmation which is shown when the page is about to close (if `blockClosing` parameter is enabled).

Also, we consider `Ctrl+Q` shortcut as frequently used, so we mapped it to the `F1` button.

It seems that we can't do anything more about that, at least in a normal browser window.

The proposed **workaround** here is to you the feature of browsers called [PWA](https://en.wikipedia.org/wiki/Progressive_web_application). It's a way to install a web page as a separate application. We've tested it in Chrome and in this mode, all the tested shortcuts are handled by Projector, not by the browser. The instructions are as follows: simply create a shortcut by selecting `Menu` | `More Tools` | `Create Shortcut...` and `Open as window`. Instructions with screenshots can be googled, for example, [this one](https://ccm.net/faq/9934-create-a-desktop-shortcut-on-google-chrome).

### Incomplete clipboard synchronization
There are some limitations with clipboard.

#### To-server
When your clipboard is changed on the client side, the server needs to apply the change on its side.

We implement it on the client side via setting ["paste" listener](https://developer.mozilla.org/en-US/docs/Web/API/Element/paste_event). So clipboard is updated on the server only if you invoke that listener, for example, by hitting Ctrl+V or Ctrl+Shift+V. **If you have an application on the server side with a "paste" button, a click on it can paste outdated information unless the listener wasn't invoked**.

Unfortunately, we can't just continuously get clipboard data from [`window.navigator.clipboard`](https://developer.mozilla.org/en-US/docs/Web/API/Navigator/clipboard) and send it to the server because when it's invoked not from user's context, there will be alert from the browser like "the site wants to read clipboard info, do you grant?".

#### To-client
It's vice versa: when your clipboard is changed on the server side, the client needs to apply the change on its side.

We set the clipboard on the client side via [`window.navigator.clipboard`](https://developer.mozilla.org/en-US/docs/Web/API/Navigator/clipboard). **This doesn't work in [insecure contexts](https://developer.mozilla.org/en-US/docs/Web/Security/Secure_Contexts/features_restricted_to_secure_contexts), so the client needs to be opened using HTTPS or on localhost to support this**.

We can't use ["copy" listener](https://developer.mozilla.org/en-US/docs/Web/API/Element/copy_event) because when this event is generated, we don't have a message from the server with actual clipboard data yet. Also, this method won't work if you click a "copy" button in your application.

### It's not possible to connect to insecure WebSocket from a secure web page
This is a limitation of browsers. So for example you can't use GitHub Pages distribution to access an insecure server.
