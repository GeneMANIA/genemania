app.factory('$$email', 
['$http',
function( $http ){

  // name : name of person sending the email (from)
  // from : email address
  // subject : email subject
  // message : string body of message
  var $$email = function( opts ){
    if( !opts.message ){
      return Promise.reject('A message must be specified to send an email');
    }

    return $http.post( config.service.baseUrl + 'mail', opts )
      .then(function( res ){
        return res.data;
      })
    ;
  };

  return $$email;

}]);