export async function addHandlers({ onPlay, onPause, onNext, onPrevious }) {
  if ("mediaSession" in navigator) {
    onPlay && navigator.mediaSession.setActionHandler("play", onPlay);
    onPause && navigator.mediaSession.setActionHandler("pause", onPause);
    onNext && navigator.mediaSession.setActionHandler("nexttrack", onNext);
    onPrevious &&
      navigator.mediaSession.setActionHandler("previoustrack", onPrevious);
  }
}

export function setMusic(music) {
  if ("mediaSession" in navigator) {
    navigator.mediaSession.metadata = new window.MediaMetadata({
      title: music.title,
      artist: music.channel,
      album: undefined,
      artwork: [
        {
          src: music.thumbnail || require("../../assets/thumbnail_default.png")
        }
      ]
    });
  }
}
