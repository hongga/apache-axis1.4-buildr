require 'src/main/resources/repositories'
require 'src/main/resources/artifacts'

define 'apache-axis1.4-buildr' do
	project.version = "0.1.0"
	eclipse.natures :java

	compile.with AXIS, COMMONS, JAVAX, LOG4J, WSDL4J
end
