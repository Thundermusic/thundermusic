/* eslint-disable no-undef */

export let musics = null;
export let callbacks = {
  downloads: {},
  media: {}
};

export function init() {
  document.addEventListener("deviceready", () => {
    listen();
    cordova.exec(() => {}, () => {}, "Thundermusic", "init");

    setInterval(() => {
      cordova.exec(() => {}, () => {}, "Thundermusic", "position");
    }, 500);
  });
}

function listen() {
  cordova.exec(
    event => {
      const first = musics == null;

      switch (event.type) {
        case "error":
          console.error("Error from plugin : " + event.error);
          break;
        case "update":
          musics = event.songs;

          if (first && callbacks.musics) {
            callbacks.musics();
          } else {
            import("../../store").then(store =>
              store.default.commit("musics/SET_MUSICS", event.songs)
            );
          }

          break;
        case "downloads":
          for (let cbId of Object.keys(callbacks.downloads)) {
            let song = null;
            for (let s of event.downloads) {
              if (s.id === cbId) {
                song = s;
                break;
              }
            }

            if (song == null) {
              callbacks.downloads[cbId](-4);
              delete callbacks.downloads[cbId];
            } else {
              callbacks.downloads[cbId](song.progress);
            }
          }
          break;
        case "handler":
          if (callbacks.media[event.handler]) {
            if (event.handler === "onPositionChange") {
              callbacks.media["onPositionChange"](
                event.position / 1000,
                event.duration / 1000
              );
            } else {
              callbacks.media[event.handler]();
            }
          }

          break;
      }

      listen();
    },
    () => {},
    "Thundermusic",
    "listen"
  );
}
