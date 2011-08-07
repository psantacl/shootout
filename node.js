var http = require('http');
http.createServer(function (req, res) {
    res.writeHead(200, {'Content-Type': 'text/plain'});
    var i = 2 + 2;
    res.end('Chicken' + i);
    //console.log('Got a request!');
    }).listen(8000);
console.log('Server running at http://127.0.0.1:8000/');
