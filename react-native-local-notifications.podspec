require 'json'
package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

firebase_sdk_version = '~> 7.10.0'

Pod::Spec.new do |s|
  s.name         = package['name']
  s.version      = package["version"]
  s.description  = package["description"]
  s.summary      = package["description"]
  s.license      = package['license']
  s.authors      = package['author']
  s.homepage     = package['homepage']
  s.platform     = :ios, "9.0"
  s.source       = { :git => "https://github.com/thanhzusu/react-native-local-notifications.git", :tag => "v#{s.version}" }
  s.source_files = 'ios/**/*.{h,m}'

  # React Native dependencies
  s.dependency          'React'

  if defined?($FirebaseSDKVersion)
    Pod::UI.puts "#{s.name}: Using user specified Firebase SDK version '#{$FirebaseSDKVersion}'"
    firebase_sdk_version = $FirebaseSDKVersion
  end

  # Firebase dependencies
  s.dependency          'Firebase/Core', firebase_sdk_version

  s.static_framework = false
  
end
