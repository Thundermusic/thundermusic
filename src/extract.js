function capitalize(str) {
  let result = "";

  for (const c of str.split(" ")) {
    if (c.charAt(0) === "(") {
      result += "(" + c.charAt(1).toUpperCase() + c.substring(2) + " ";
    } else {
      result += c.charAt(0).toUpperCase() + c.substring(1) + " ";
    }
  }

  return result.substr(0, result.length - 1);
}

function extract(videoTitle, channel) {
  if (channel.endsWith(" - Topic")) {
    return {
      title: videoTitle,
      artist: channel.substring(0, channel.length - 8)
    };
  }

  let video = videoTitle.trim();

  while (video.indexOf("[") !== -1 && video.indexOf("]") !== -1) {
    let a = video.indexOf("["),
      b = video.indexOf("]");

    if (
      (video.length > a + 2 && video.substr(a + 1, a + 3) === "ft") ||
      (video.length > a + 4 && video.substr(a + 1, a + 5) === "feat")
    )
      continue;

    video = (video.substr(0, a) + video.substr(b + 1, video.length)).trim();
  }

  let feats = [];
  let remix = null;

  while (video.indexOf("(") !== -1 && video.indexOf(")") !== -1) {
    let a = video.indexOf("("),
      b = video.indexOf(")");

    if (
      (video.length > a + 2 && video.substr(a + 1, 2) === "ft") ||
      (video.length > a + 4 && video.substr(a + 1, 4) === "feat")
    ) {
      feats.push(video.substr(a, b - a + 1));
    }

    if (b > 3 && video.toLowerCase().substr(b - 5, 5) === "remix") {
      remix = video.substr(a, b - a + 1);
    }

    video = (
      video.substr(0, video.charAt(a - 1) === " " ? a - 1 : a) +
      video.substr(b + 1, video.length - b + 1)
    ).trim();
  }

  video = video
    .replace(/\s+/g, " ")
    .replace(/[^[(]((ft|feat)\..*)/g, (_, r) => ` (${r})`)
    .trim();

  if (video.startsWith("-")) {
    video = video.substr(1).trim();
  }

  if (video.endsWith("-")) {
    video = video.substr(video.length - 1).trim();
  }

  // 'The Chainsmokers - Side Effects (Lyric Video) ft. Emily Warren '
  // --> 'The Chainsmokers - Side Effects (ft. Emily Warren)'

  let title = null;
  let artist = null;

  const splitChars = ["-", "–", ":"];
  for (const c of splitChars) {
    if (video.indexOf(c) > 0) {
      const split = video.split(c);

      title = split[1];
      artist = capitalize(split[0]);

      break;
    }
  }

  if (title === null) {
    title = videoTitle;
  } else {
    [...feats, remix]
      .filter(s => s !== null)
      .forEach(str => (title += " " + capitalize(str.replace("(feat", "(ft"))));
  }

  if (artist == null) {
    artist = channel;
  } else {
    artist = artist
      .split(/[,&]/g)
      .concat(remix ? [remix.substring(1, remix.length - 7)] : [])
      .map(s => capitalize(s.trim()))
      .join(", ");
  }

  return { title: title.trim(), artist: artist.trim() };
}

export default function(music) {
  const extracted = extract(music.title, music.channel);

  music.title = extracted.title;
  music.channel = extracted.artist;

  return music;
}
