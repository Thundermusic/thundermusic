function capitalize(str) {
  let result = "";

  for (const c of str.split(" ")) {
    if (c.charAt(0) === "(" && c !== "(ft." && c !== "(feat.") {
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

  let feats = [];
  let remix = null;

  if (video.indexOf("ft.") !== -1 || video.indexOf("feat.") !== 1) {
    let pos = video.indexOf("ft");

    if (pos === -1) {
      pos = video.indexOf("feat.");
    }

    let sep = video.indexOf("-");

    if (sep < pos) {
      sep = video.length + 1;
    }

    if (pos > 0 && video.charAt(pos - 1) !== "(" && sep !== -1) {
      video =
        video.substring(0, pos) +
        "(" +
        video.substring(pos, sep - 1) +
        ")" +
        video.substring(sep - 1, video.length);
    }
  }

  while (video.indexOf("[") !== -1 && video.indexOf("]") !== -1) {
    let a = video.indexOf("["),
      b = video.indexOf("]");

    if (
      (video.length > a + 2 && video.substr(a + 1, a + 3) === "ft") ||
      (video.length > a + 4 && video.substr(a + 1, a + 5) === "feat")
    )
      continue;

    video = (
      video.substring(0, a) + video.substring(b + 1, video.length)
    ).trim();
  }

  while (video.indexOf("(") !== -1 && video.indexOf(")") !== -1) {
    let a = video.indexOf("("),
      b = video.indexOf(")");

    if (a > b) {
      break; // happens, it's fail
    }

    if (
      (video.length > a + 2 && video.substr(a + 1, 2) === "ft") ||
      (video.length > a + 4 && video.substr(a + 1, 4) === "feat")
    ) {
      feats.push(video.substring(a, b + 1));
    }

    if (b > 3 && video.toLowerCase().substr(b - 5, 5) === "remix") {
      remix = video.substring(a, b + 1);
    }

    video = (
      video.substr(0, video.charAt(a - 1) === " " ? a - 1 : a) +
      video.substring(b + 1, video.length)
    ).trim();
  }

  video = video
    .replace(/\s+/g, " ")
    .replace(/[^[(]((ft|feat)\..*)/g, (_, r) => ` (${r})`)
    .replace(/ \(?extended\)?/gi, "")
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
    if (title.toLowerCase().indexOf("ost") !== -1) {
      const temp = title;

      title = artist;
      artist = temp;
    }

    const ost = artist.toLowerCase().indexOf("ost");
    if (ost !== -1) {
      artist = artist.substring(0, ost + 3);
    }

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

/*export default function(music) {
  const extracted = extract(music.title, music.channel);

  music.title = extracted.title;
  music.channel = extracted.artist;

  return music;
}*/

console.log(extract("Madeon - La Lune (Audio) ft. Dan Smith", "MadeonVEVO"));
