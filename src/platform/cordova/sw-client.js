/* eslint-disable */

import { callbacks } from "./listener";

export function swDownload(getMeta) {
  return async (music, meta, progressFn) => {
    const { url } = await (meta || getMeta(music));

    cordova.exec(() => {}, err => {}, "Thundermusic", "download", [{ ...music, artist: music.channel, url }]);

    await new Promise(resolve => {
      callbacks.downloads[music.id] = progress => {
        if (progress === -2) {
          resolve();
        } else {
          progressFn(progress);
        }
      };
    });
  };
}

export function cleanupMusic(music) {
  return new Promise((resolve, reject) => {
    cordova.exec(resolve, reject, "Thundermusic", "remove", [music]);
  });
}
