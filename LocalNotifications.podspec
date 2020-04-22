require 'json'
package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

firebase_sdk_version = '~> 6.13.0'

Pod::Spec.new do |s|
  s.name                = "LocalNotifications"
  s.version             = package["version"]
  s.description         = package["description"]
  s.summary             = <<-DESC
                            A local push notification implementation for React Native, supporting iOS & Android.
                          DESC
  s.license             = package['license']
  s.authors             = package['author']
  s.ios.deployment_target = "9.0"
  s.source_files        = 'ios/**/*.{h,m}'

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
