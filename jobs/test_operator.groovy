job("operator test") {
  steps {
    
    bitesizeDeploy {
      endpoint 'http://endpoint'
      token 'token'
      version '1.0'
	  	service 'svc-one'
    	application 'test-app'
    }
  }
}
