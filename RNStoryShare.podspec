require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "RNStoryShare"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.author       = { package["author"]["name"] => package["author"]["email"] }
  s.platform     = :ios, "8.0"
  s.source       = { :git => "https://github.com/author/RNStoryShare.git", :tag => "master" }

  s.source_files  = "ios/**/*.{h,m}"

  s.dependency "React"
end
