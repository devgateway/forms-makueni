const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function (app) {
  app.use(
    createProxyMiddleware((path) => !path.match('^/ui/'), {
      target: 'http://localhost:8090', logLevel: 'debug',
    }),
  );
};
