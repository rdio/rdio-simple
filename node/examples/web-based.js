var Hapi = require('hapi'),
    Rdio = require('./../rdio'),
    RdioCredentials = require('./rdio_consumer_credentials');

var options = {
  maxCookieSize: 0,
  cookieOptions: {
    password: 'some secure password',
    isSecure: false
  }
};

var server = new Hapi.Server(8080);
server.pack.allow({ ext: true }).require('yar', options, function (err) {
  if (err) {
    console.log(err);
    throw err;
  }
});

server.route({
  method: 'GET',
  path: '/',
  handler: function (request) {
    var accessToken = request.session.get('accessToken');
    if (accessToken) {
      var rdio = new Rdio([RdioCredentials.RDIO_CONSUMER_KEY, RdioCredentials.RDIO_CONSUMER_SECRET],
                          [accessToken.token, accessToken.secret]);
      rdio.call('currentUser', function(err, data) {
        if (err) {
          console.log(err);
          request.reply(new Error("Error getting current user"));
        }

        var currentUser = data.result;

        rdio.call('getPlaylists', function(err, data) {
          if (err) {
            console.log(err);
            request.reply(new Error("Error getting playlists"));
          }

          var playlists = data.result.owned;

          var body = [];
          body.push('<html><head><title>Rdio-Simple Example</title></head><body>');
          body.push('<p>' + currentUser.firstName + "'s playlists:</p>");
          body.push('<ul>');

          playlists.forEach(function(playlist) {
            body.push('<li><a href="' + playlist.shortUrl + '">' + playlist.name + '</a></li>');
          });
          body.push('</ul>');
          body.push('<a href="/logout">Log out of Rdio</a></body></html>');

          request.reply(body.join('\n'));
        });
      });
    } else {
      var body = [];

      body.push('<html><head><title>Rdio-Simple Example</title></head><body>');
      body.push('<a href="/login">Log into Rdio</a>');
      body.push('</body></html>');

      request.reply(body.join('\n'));
    }
  }
});

server.route({
  method: 'GET',
  path: '/login',
  handler: function(request) {
    // begin the authentication process
    var rdio = new Rdio([RdioCredentials.RDIO_CONSUMER_KEY, RdioCredentials.RDIO_CONSUMER_SECRET]);
    var callbackUrl = request.server.info.protocol + "://" + request.info.host + "/callback";
    rdio.beginAuthentication(callbackUrl, function(err, authUrl) {
      if (err) {
        console.log(err);
        request.reply(new Error("Error beginning authentication"));
      }

      request.session.set('requestToken', {
        token: rdio.token[0],
        secret: rdio.token[1]
      });

      request.reply.redirect(authUrl);
    });
  }
});


server.route({
  method: 'GET',
  path: '/callback',
  handler: function(request) {
    var requestToken = request.session.get('requestToken');
    var verifier = request.query.oauth_verifier;

    if (requestToken && verifier) {
      // exchange the request token and verifier for an access token.
      var rdio = new Rdio([RdioCredentials.RDIO_CONSUMER_KEY, RdioCredentials.RDIO_CONSUMER_SECRET],
                          [requestToken.token, requestToken.secret]);
      rdio.completeAuthentication(verifier, function(err) {
        if (err) {
          console.log(err);
          request.reply(new Error("Error completing Authentication"));
        }

        request.session.set('accessToken', {
          token: rdio.token[0],
          secret: rdio.token[1]
        });
        request.session.clear('requestToken');

        request.reply.redirect('/');
      });
    } else {
      request.reply.redirect('/logout');
    }
  }
});


server.route({
  method: 'GET',
  path: '/logout',
  handler: function(request) {
    request.session.reset();
    request.reply.redirect('/');
  }
});



server.start(function() {
  console.log('Server started on port:', server.info.port);
});
