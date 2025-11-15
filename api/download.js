module.exports = async (req, res) => {
  const apkUrl = process.env.APK_DOWNLOAD_URL;

  if (!apkUrl) {
    res.status(500).send("APK download URL is not configured.");
    return;
  }

  res.writeHead(302, { Location: apkUrl });
  res.end();
};
