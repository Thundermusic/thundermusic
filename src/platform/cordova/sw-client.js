import { callbacks } from "./listener";

export function swDownload(getMeta) {
  return async (music, meta, progressFn) => {
    const { url } = await (meta || getMeta(music));

    // eslint-disable-next-line no-undef
    cordova.exec(() => {}, () => {}, "Thundermusic", "download", [
      { ...music, artist: music.artist, url }
    ]);

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
    // eslint-disable-next-line no-undef
    cordova.exec(resolve, reject, "Thundermusic", "remove", [music]);
  });
}
