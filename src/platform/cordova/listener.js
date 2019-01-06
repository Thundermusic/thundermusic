/* eslint-disable no-undef */

export let musics = null;
export let downloads = {};
export let callbacks = {};

export function init() {
  document.addEventListener("deviceready", () => {
    listen();
    cordova.exec(
      () => {
        console.log("init is gud");
      },
      err => {
        console.log("init no gud : " + err);
      },
      "Thundermusic",
      "init"
    );
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
          }

          break;
        case "downloads":
          downloads = event.downloads;

          if (callbacks.downloads) {
            callbacks.downloads();
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
