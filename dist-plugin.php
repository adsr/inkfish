<?php

/**
 * This script makes a plugin jar for each *Plugin.class in bin-plugin/ and
 * places it in the dist/plugins/ directory. For example, if TestPlugin.class
 * and TestObject.class are files in bin-plugin/, these will included in
 * Test.jar and placed in dist/plugins/. A manifest attribute, Plugin-Class,
 * pointing to the TestPlugin class will also be written to the jar. Inkfish
 * will then attempt to load TestPlugin as an implementation of InkfishPlugin.
 *
 * Usage:
 *     php dist-plugin.php [-v]
 *
 * @author Adam Saponara
 */

// Directory separator for shell_exec commands
$sep = DIRECTORY_SEPARATOR;

// Verbose mode?
$verbose = in_array('-v', $_SERVER['argv']) || in_array('--verbose', $_SERVER['argv']) ? 'v' : '';

// Make dist/ and dist/plugins/ if not present
if (!is_dir('dist')) mkdir('dist');
if (!is_dir('dist/plugins')) mkdir('dist/plugins');

// For each *Plugin.class in bin-plugin/ ...
$plugins = glob("bin-plugin/*Plugin.class");
if (empty($plugins)) {
	die('No class files found in bin-plugin/ directory. Compile the plugins first.');
}
foreach ($plugins as $plugin) {

	// Substract "Plugin.class" from the end of the string
	$pluginroot = substr(basename($plugin), 0, -12);
	
	// Set target jar path
	$jarpath = "dist{$sep}plugins{$sep}{$pluginroot}.jar";
	
	// Make jar
	echo "Making $jarpath\n";
	echo shell_exec("jar {$verbose}cf $jarpath bin-plugin{$sep}{$pluginroot}*.class");
	
	// Add Plugin-Class attribute to manifest
	file_put_contents('temp.mf', "Plugin-Class: {$pluginroot}Plugin\n");
	echo shell_exec("jar {$verbose}umf temp.mf $jarpath");
}

// Delete temp.mf if it exists
if (file_exists('temp.mf')) unlink('temp.mf');
