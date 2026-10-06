#!/usr/bin/env ruby
# Adds the TuIndiceUITests UI-test target, its UITests group and its shared scheme to
# TuIndiceHost.xcodeproj. Idempotent: running it again changes nothing. The existing TuIndiceHost
# target and scheme are never touched, and the Podfile is not involved.
#
# Usage, from iosApp/: LANG=en_US.UTF-8 ruby scripts/add-ui-test-target.rb

require 'xcodeproj'

TARGET_NAME = 'TuIndiceUITests'.freeze
APP_TARGET_NAME = 'TuIndiceHost'.freeze
PHASE_NAME = 'Build Scenario Kit'.freeze

root = File.expand_path('..', __dir__)
project = Xcodeproj::Project.open(File.join(root, 'TuIndiceHost.xcodeproj'))

app = project.targets.find { |t| t.name == APP_TARGET_NAME } or abort("missing target #{APP_TARGET_NAME}")

ui = project.targets.find { |t| t.name == TARGET_NAME }

if ui.nil?
	ui = project.new_target(:ui_test_bundle, TARGET_NAME, :ios, '16.0')
	ui.add_dependency(app)

	# new_target links a stale SDK Foundation.framework; the bundle needs no explicit framework.
	ui.frameworks_build_phase.files.each(&:remove_from_project)
	project.frameworks_group.children.find { |g| g.display_name == 'iOS' }&.remove_from_project
	ui.build_configuration_list.default_configuration_name = 'Debug'

	group = project.main_group.new_group('UITests', 'UITests')
	Dir[File.join(root, 'UITests', '*.swift')].sort.each do |file|
		ui.add_file_references([group.new_file(File.basename(file))])
	end
	generated = group.new_group('Generated', 'Generated')
	Dir[File.join(root, 'UITests', 'Generated', '*.swift')].sort.each do |file|
		ui.add_file_references([generated.new_file(File.basename(file))])
	end

	config_group = project.main_group.children.find { |g| g.display_name == 'Config' } or abort('missing Config group')
	xcconfig = config_group.new_file('UITests.xcconfig')
	ui.build_configurations.each do |configuration|
		configuration.base_configuration_reference = xcconfig
		configuration.build_settings.clear
	end

	scripts_group = project.main_group.children.find { |g| g.display_name == 'scripts' } or abort('missing scripts group')
	scripts_group.new_file('build-scenario-kit.sh')

	phase = ui.new_shell_script_build_phase(PHASE_NAME)
	phase.shell_script = "\"${SRCROOT}/scripts/build-scenario-kit.sh\"\n"
	phase.always_out_of_date = '1'
	ui.build_phases.move(phase, 0)

	attributes = (project.root_object.attributes['TargetAttributes'] ||= {})
	attributes[ui.uuid] = { 'CreatedOnToolsVersion' => '27.0', 'TestTargetID' => app.uuid }

	project.save
	puts "Added target #{TARGET_NAME}."
else
	puts "Target #{TARGET_NAME} already present."
end

scheme_path = File.join(Xcodeproj::XCScheme.shared_data_dir(project.path), "#{TARGET_NAME}.xcscheme")
if File.exist?(scheme_path)
	puts "Scheme #{TARGET_NAME} already present."
else
	scheme = Xcodeproj::XCScheme.new
	scheme.add_build_target(app)
	scheme.add_build_target(ui, false)
	scheme.add_test_target(ui)
	scheme.set_launch_target(app)
	scheme.test_action.build_configuration = 'Debug'
	scheme.save_as(project.path, TARGET_NAME, true)
	puts "Added scheme #{TARGET_NAME}."
end
