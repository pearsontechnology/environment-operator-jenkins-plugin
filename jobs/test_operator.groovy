job("operator test") {
  steps {
    
    deploy_to_environment {
      endpoint 'http://endpoint'
      token 'token'
      version '1.0'
	  	service 'svc-one'
    	application 'test-app'
    }
  }
}
