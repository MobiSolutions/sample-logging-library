<?php

$httpStatusCode = 200;
$httpStatusMsg  = 'OK';

if (rand(1, 10) <= 2) {
    $httpStatusCode = 400;
    $httpStatusMsg  = 'Bad request';
}

$phpSapiName    = substr(php_sapi_name(), 0, 3);
if ($phpSapiName == 'cgi' || $phpSapiName == 'fpm') {
    header('Status: '.$httpStatusCode.' '.$httpStatusMsg);
} else {
    $protocol = isset($_SERVER['SERVER_PROTOCOL']) ? $_SERVER['SERVER_PROTOCOL'] : 'HTTP/1.0';
    header($protocol.' '.$httpStatusCode.' '.$httpStatusMsg);
}
echo "{}";
?>

