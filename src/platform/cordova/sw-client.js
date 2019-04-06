import { callbacks } from "./listener";

export function swDownload() {
  return async (music, meta, progressFn) => {
    // eslint-disable-next-line no-undef
    cordova.exec(() => {}, () => {}, "Thundermusic", "download", [
      { ...music, artist: music.artist }
    ]);

    await new Promise(resolve => {
      callbacks.downloads[music.id] = progress => {
        if (progress === -4) {
          resolve();
        } else {
          progressFn(progress / 100);
        }
      };
    });
  };
}

export function cleanupMusic(music) {
  return new Promise((resolve, reject) => {
    // eslint-disable-next-line no-undef
    cordova.exec(resolve, reject, "Thundermusic", "remove", [music]);
  });
}
