http_request = http('http://localhost:8080/pluginManager/api/json?depth=1&xpath=/*/*/shortName|/*/*/version&wrapper=plugins',
              auth: {user: 'admin', pass: 'admin'},
              headers: {'Content-Type' => 'application/json'})

describe json(content: http_request.body) do
  its(['plugins','last','enabled']) { should eq true }
  its(['plugins','last','shortName']) { should eq "environment-operator-deployer" }
end