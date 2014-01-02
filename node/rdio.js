var qs = require("querystring"),
    url = require("url"),
    http = require("http"),
    om = require("./om");

module.exports = Rdio;

function Rdio(consumer, token) {
    this.consumer = consumer;
    this.token = token;
}

Rdio.prototype.beginAuthentication = function beginAuthentication(callbackUrl, callback) {
    var self = this;

    this._signedPost("http://api.rdio.com/oauth/request_token", {
        oauth_callback: callbackUrl
    }, function (err, body) {
        if (err) {
            callback(err, null);
        } else {
            var parsed = qs.parse(body);
            var token = [parsed.oauth_token, parsed.oauth_token_secret];
            var authUrl = parsed.login_url + "?oauth_token=" + parsed.oauth_token;
            // Save the token.
            self.token = token;
            // Call the callback with a URL the app can use to auth.
            callback(null, authUrl);
        }
    });
};

Rdio.prototype.completeAuthentication = function completeAuthentication(verifier, callback) {
    var self = this;

    this._signedPost("http://api.rdio.com/oauth/access_token", {
        oauth_verifier: verifier
    }, function (err, body) {
        if (err) {
            callback(err);
        } else {
            var parsed = qs.parse(body);
            var token = [parsed.oauth_token, parsed.oauth_token_secret];
            // Save the token.
            self.token = token;
            // Call the callback.
            callback(null);
        }
    });
};

Rdio.prototype.call = function call(method, params, callback) {
    if (typeof params == "function") {
        callback = params;
        params = null;
    }

    var copy = {};

    if (params) {
        for (var param in params) {
            copy[param] = params[param];
        }
    }

    copy.method = method;

    this._signedPost("http://api.rdio.com/1/", copy, function (err, body) {
        if (err) {
            callback(err);
        } else {
            callback(null, JSON.parse(body));
        }
    });
};

Rdio.prototype._signedPost = function signedPost(urlString, params, callback) {
    var auth = om(this.consumer, urlString, params, this.token);
    var parsed = url.parse(urlString);
    var content = qs.stringify(params);

    var req = http.request({
        method: "POST",
        host: parsed.host,
        port: parsed.port || "80",
        path: parsed.pathname,
        headers: {
            "Authorization": auth,
            "Content-Type": "application/x-www-form-urlencoded",
            "Content-Length": content.length.toString()
        }
    }, function (res) {
        var body = "";
        
        res.setEncoding("utf8");
        
        res.on("data", function (chunk) {
            body += chunk;
        });

        res.on("end", function () {
            var data = {};
            
            try {
                data = JSON.parse(body);
            } catch(e) {
                data = qs.parse(body);

                if (!data.oauth_token) {
                    data.status = 'error';
                    data.message = body;
                }
            }

            if (data.status === 'error') {
                callback(data.message);
            } else {
                callback(null, body);
            }
        });
    });

    req.on("error", function (err) {
        callback(err);
    });

    req.end(content);
};
