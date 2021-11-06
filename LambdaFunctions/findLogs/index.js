const AWS = require('aws-sdk');
const s3 = new AWS.S3();
exports.handler = async (event) => {
  const Bucket = 'fake-log-file';
  const Key = 'log.txt';
  const data = await s3.getObject({ Bucket, Key }).promise();
  const textfile = data.Body.toString('ascii').split("\n");
  const end = textfile.length-1;
  const logStart =  convertTime(event["start"]);
  const index = searchForStart(textfile, logStart, 0, end);

  let logs = "";
  const endTime = parseInt(logStart)+parseInt(event["interval"]);
  const logsFound = getLogs(logs, textfile, logStart, index, endTime);
  return logsFound;
};

function searchForStart(arr, logStart, start, end)
{
    if(start > end) return false;
    let mid = Math.floor((start + end)/2);
    if(convertTime(arr[mid]) == logStart) return mid;
    if(convertTime(arr[mid]) > logStart) return searchForStart(arr, logStart, start, mid-1);
    else return searchForStart(arr, logStart, mid+1, end);
}

function convertTime(str)
{
  return str.slice(0,2) + str.slice(3, 5) + str.slice(6, 8) + str.slice(9, 12);
}

function getLogs(logs, textfile,  time, index, end)
{
  if(time > end)
    return logs;
  const log = textfile[index];
  logs += log + "/n";

  time = convertTime(log.split(" ")[0]);
  index = index + 1;
  return getLogs(logs, textfile, time, index, end);
}