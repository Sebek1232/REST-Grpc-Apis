const AWS = require('aws-sdk');
const s3 = new AWS.S3();

exports.handler = async (event, context) => {
  const Bucket = 'fake-log-file'
  const Key = 'log.txt'
  const data = await s3.getObject({ Bucket, Key }).promise();
  const textfile = data.Body.toString('ascii').split("\n");
  const firstLine = textfile[0].split(" ");
  const start = firstLine[0];
  const lastLine = textfile[textfile.length-2].split(" ");
  const end = lastLine[0];

  const startNum = convertTime(start)
  const endNum = convertTime(end)

  const startArg = convertTime(event["start"]);
  if(startArg >= startNum && startArg <= endNum)
    return 1;
  else
    return 0;
};

function convertTime(str)
{
  return str.slice(0,2) + str.slice(3, 5) + str.slice(6, 8) + str.slice(9, 12);
}