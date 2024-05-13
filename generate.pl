#!/usr/bin/perl -w

use strict;

my $generatorDirectory = "g8templates";
my $g8Directory = ".g8";
my @availableComponents = ("big decimal input", "character count", "yes-no radio input");

sub getNextComponent {
  print "Choose the next component (0 to finish):" . "\n";

  my $index = 0;

  foreach(@availableComponents) {
    $index = $index + 1;
    print "$index - $_\n";
  }

  my $choice = "";

  while (!($choice =~ /^\d+$/) || $choice < 0 || $choice > @availableComponents) {
    $choice = <STDIN>;
    chomp $choice;
  }

  return $choice;
}

sub getComponentsFromUser {
  my @selectedComponents = ();
  my $nextComponent = -1;

  while ($nextComponent != 0 || @selectedComponents == 0) {
    $nextComponent = getNextComponent();

    if ($nextComponent != 0) {
      push @selectedComponents, $availableComponents[$nextComponent - 1];
      print "Selected so far: " . join(", ", @selectedComponents) . "\n";
    }
  }

  return @selectedComponents;
}

sub getTemplateName {
  my $templateName = "";

  while ($templateName eq "") {
    print "Name the directory for the new template" . "\n";
    $templateName = <STDIN>;
    chomp $templateName;
  }

  return $templateName;
}

sub readFileToString($) {
  my $filename = shift;
  open(my $fh, '<', $filename) or die "Couldn't open $filename for reading: $!\n";
  my $string = do { local($/); <$fh> };
  close($fh);

  return $string;
}

sub performSubs($@) {
}

sub writeFile($$) {
  my ($filename, $string) = @_;

  open(my $fh, '>', $filename) or die "Couldn't open $filename for writing: $!\n";
  print $fh $string;
  close($fh);
}

sub createControllerTemplate($$) {
  my $templateName = shift;
  my $components = shift;

  mkdir("$g8Directory/$templateName/app/controllers", 0755) or die "Couldn't create controllers directory";
  mkdir("$g8Directory/$templateName/app/controllers/\$section\$", 0755) or die "Couldn't create controllers' section directory";
  
  my $controllerTemplate = readFileToString("$generatorDirectory/controller_template.scala");

  writeFile("$g8Directory/$templateName/app/controllers/\$section\$/\$className\$Controller.scala", $controllerTemplate) or die "Couldn't write controller g8 template";
}

sub createFormProviderTemplate($$) {
  my $templateName = shift;
  my $components = shift;

  mkdir("$g8Directory/$templateName/app/forms", 0755) or die "Couldn't create forms directory";
  mkdir("$g8Directory/$templateName/app/forms/\$section\$", 0755) or die "Couldn't create form's section directory";
  
  my $formProviderTemplate = readFileToString("$generatorDirectory/formprovider_template.scala");

  my @imports = ();
  my @mappings = ();
  my @limits = ();

  my $index = 0;

  foreach (@$components) {
    my $component = $_;

    $index = $index + 1;

    my $importsFile = readFileToString("$generatorDirectory/$component/formprovider_imports.scala");
    my $mappingsFile = readFileToString("$generatorDirectory/$component/formprovider_mappings.scala");
    chomp $mappingsFile;
    my $limitsFile = readFileToString("$generatorDirectory/$component/formprovider_limits.scala");
    chomp $limitsFile;
 
    my $fieldName = "\$field" . $index . "Name\$";
    my $maxDp = "\$field" . $index . "MaxDp\$";
    my $minValue = "\$field" . $index . "Min\$";
    my $maxValue = "\$field" . $index . "Max\$";
    my $maxLength = "\$field" . $index . "Length\$";

    $mappingsFile =~ s/\$pre_fieldname\$/$fieldName/g;
    $limitsFile =~ s/\$pre_fieldname\$/$fieldName/g;
    $limitsFile =~ s/\$pre_maxdp\$/$maxDp/g;
    $limitsFile =~ s/\$pre_min\$/$minValue/g;
    $limitsFile =~ s/\$pre_max\$/$maxValue/g;
    $limitsFile =~ s/\$pre_length\$/$maxLength/g;

    push(@imports, $importsFile);
    push(@mappings, $mappingsFile);
    push(@limits, $limitsFile);
  }

  my %importsHash = map { $_ => 1} @imports;
  delete %importsHash{""};
  my $importsToReplace = join("\n", keys %importsHash);
  chomp $importsToReplace;
  my $mappingsToReplace = join(",\n", @mappings);
  my $limitsToReplace = join("\n", @limits);

  chomp $importsToReplace;
  chomp $mappingsToReplace;
  chomp $limitsToReplace;

  if ($importsToReplace ne "") {
    $importsToReplace = "\n" . $importsToReplace;
  }

  if ($mappingsToReplace ne "") {
    $mappingsToReplace = "\n" . $mappingsToReplace;
  }


  if ($limitsToReplace ne "") {
    $limitsToReplace = "\n" . $limitsToReplace;
  }

  $formProviderTemplate =~ s/\$pre_imports\$/$importsToReplace/;
  $formProviderTemplate =~ s/\$pre_mappings\$/$mappingsToReplace/;
  $formProviderTemplate =~ s/\$pre_limits\$/$limitsToReplace/;

  writeFile("$g8Directory/$templateName/app/forms/\$section\$/\$className\$FormProvider.scala", $formProviderTemplate) or die "Couldn't write formProvider g8 template";
}

sub createModelClassTemplate($$) {
  my $templateName = shift;
  my $components = shift;

  mkdir("$g8Directory/$templateName/app/models", 0755) or die "Couldn't create models directory";
  mkdir("$g8Directory/$templateName/app/models/\$section\$", 0755) or die "Couldn't create model's section directory";
  
  my $modelTemplate = readFileToString("$generatorDirectory/model_template.scala");

  my @imports = ();
  my @fields = ();

  my $index = 0;

  foreach (@$components) {
    my $component = $_;

    $index = $index + 1;

    my $importsFile = readFileToString("$generatorDirectory/$component/modelclass_imports.scala");
    my $fieldsFile = readFileToString("$generatorDirectory/$component/modelclass_fields.scala");
    chomp $fieldsFile;
 
    my $fieldName = "\$field" . $index . "Name\$";

    $fieldsFile =~ s/\$pre_fieldname\$/$fieldName/g;

    push(@imports, $importsFile);
    push(@fields, $fieldsFile);
  }

  my $importsToReplace = join("\n", @imports);
  my $fieldsToReplace = join(", ", @fields);

  chomp $importsToReplace;

  if ($importsToReplace ne "") {
    $importsToReplace = "\n" . $importsToReplace;
  }

  $modelTemplate =~ s/\$pre_modelImports\$/$importsToReplace/;
  $modelTemplate =~ s/\$pre_modelFields\$/$fieldsToReplace/;

  writeFile("$g8Directory/$templateName/app/models/\$section\$/\$className\$.scala", $modelTemplate) or die "Couldn't write model g8 template";
}

sub createPageTemplate($) {
  my $templateName = shift;

  mkdir("$g8Directory/$templateName/app/pages", 0755) or die "Couldn't create pages directory";
  mkdir("$g8Directory/$templateName/app/pages/\$section\$", 0755) or die "Couldn't create pages' section directory";
  
  my $pageTemplate = readFileToString("$generatorDirectory/page_template.scala");

  writeFile("$g8Directory/$templateName/app/pages/\$section\$/\$className\$Page.scala", $pageTemplate) or die "Couldn't write model g8 template";
}

sub createCheckAnswersTemplate($$) {
  my $templateName = shift;
  my $components = shift;

  mkdir("$g8Directory/$templateName/app/viewmodels", 0755) or die "Couldn't create viewmodels directory";
  mkdir("$g8Directory/$templateName/app/viewmodels/checkAnswers", 0755) or die "Couldn't create viewmodel's checkAnswers directory";
  mkdir("$g8Directory/$templateName/app/viewmodels/checkAnswers/\$section\$", 0755) or die "Couldn't create viewmodel's section directory";
  
  my $checkanswersTemplate = readFileToString("$generatorDirectory/checkanswers_template.scala");

  my @summaries = ();

  my $index = 0;

  foreach (@$components) {
    my $component = $_;

    $index = $index + 1;

    my $cyaSummaryFile = readFileToString("$generatorDirectory/$component/cya_summary.scala");
    chomp $cyaSummaryFile;
 
    my $fieldName = "\$field" . $index . "Name\$";

    $cyaSummaryFile =~ s/\$pre_fieldname\$/$fieldName/g;

    push(@summaries, $cyaSummaryFile);
  }

  my $summariesToReplace = join(" + \"<BR>\" + ", @summaries);

  if ($summariesToReplace eq "") {
    $summariesToReplace = "\"\"";
  }

  $checkanswersTemplate =~ s/\$pre_cyaSummary\$/$summariesToReplace/;

  writeFile("$g8Directory/$templateName/app/viewmodels/checkAnswers/\$section\$/\$className\$Summary.scala", $checkanswersTemplate) or die "Couldn't write checkanswers g8 template";
}

sub createControllerSpecTemplate($$) {
  my $templateName = shift;
  my $components = shift;

  mkdir("$g8Directory/$templateName/generated-test/controllers", 0755) or die "Couldn't create test controllers directory";
  mkdir("$g8Directory/$templateName/generated-test/controllers/\$section\$", 0755) or die "Couldn't create test controller's section directory";
  
  my $controllerSpecTemplate = readFileToString("$generatorDirectory/controller_spec_template.scala");

  my @examples = ();
  my @fieldsToExamples = ();
  my @fieldsCSV = ();
  my @fieldKeyValuePairs = ();

  my $index = 0;

  foreach (@$components) {
    my $component = $_;

    $index = $index + 1;

    my $examplesFile = readFileToString("$generatorDirectory/$component/controller_spec_examples.scala");
    my $fieldsToExamplesFile = readFileToString("$generatorDirectory/$component/controller_spec_fieldstoexample.scala");
    chomp $fieldsToExamplesFile;
 
    my $fieldKeyValuePairs = readFileToString("$generatorDirectory/$component/controller_spec_fieldkeyvaluepairs.scala");
    chomp $fieldKeyValuePairs;
 
    my $fieldName = "\$field" . $index . "Name\$";

    $examplesFile =~ s/\$pre_fieldname\$/$fieldName/g;
    $examplesFile =~ s/\$pre_index\$/$index/g;
    $fieldsToExamplesFile =~ s/\$pre_fieldname\$/$fieldName/g;
    $fieldKeyValuePairs =~ s/\$pre_fieldname\$/$fieldName/g;

    push(@examples, $examplesFile);
    push(@fieldsToExamples, $fieldsToExamplesFile);
    push(@fieldsCSV, $fieldName);
    push(@fieldKeyValuePairs, $fieldKeyValuePairs);
  }

  my $examplesToReplace = join("", @examples);
  chomp $examplesToReplace;

  my $fieldsToExamples = join(",\n", @fieldsToExamples);
  chomp $fieldsToExamples;

  my $fieldKeyValuePairs = join(", ", @fieldKeyValuePairs);
  chomp $fieldKeyValuePairs;

  my $fieldsCSV = join(", ", @fieldsCSV);

  if ($examplesToReplace ne "") {
    $examplesToReplace = "\n" . $examplesToReplace;
  }

  if ($fieldsToExamples ne "") {
    $fieldsToExamples = "\n" . $fieldsToExamples;
  }

  $controllerSpecTemplate =~ s/\$pre_fieldsExamples\$/$examplesToReplace/;
  $controllerSpecTemplate =~ s/\$pre_fieldsToExamples\$/$fieldsToExamples/;
  $controllerSpecTemplate =~ s/\$pre_fieldsCSV\$/$fieldsCSV/;
  $controllerSpecTemplate =~ s/\$pre_fieldKeyValuePairs\$/$fieldKeyValuePairs/;

  writeFile("$g8Directory/$templateName/generated-test/controllers/\$section\$/\$className\$ControllerSpec.scala", $controllerSpecTemplate) or die "Couldn't write controller spec g8 template";
}

sub createFormProviderSpecTemplate($$) {
  my $templateName = shift;
  my $components = shift;

  mkdir("$g8Directory/$templateName/generated-test/forms", 0755) or die "Couldn't create test forms directory";
  mkdir("$g8Directory/$templateName/generated-test/forms/\$section\$", 0755) or die "Couldn't create test forms' section directory";
  
  my $formProviderSpecTemplate = readFileToString("$generatorDirectory/formprovider_spec_template.scala");

  my @imports = ();
  my @extensions = ();
  my @tests = ();

  my $index = 0;

  foreach (@$components) {
    my $component = $_;

    $index = $index + 1;

    my $importsFile = readFileToString("$generatorDirectory/$component/formprovider_spec_imports.scala");
    chomp $importsFile;

    my $extensionsFile = readFileToString("$generatorDirectory/$component/formprovider_spec_extensions.scala");
    chomp $extensionsFile;

    my $testFile = readFileToString("$generatorDirectory/$component/formprovider_spec_tests.scala");
 
    my $fieldName = "\$field" . $index . "Name\$";

    $testFile =~ s/\$pre_fieldname\$/$fieldName/g;
    $testFile =~ s/\$pre_index\$/$index/g;

    push(@imports, $importsFile);
    push(@extensions, $extensionsFile);
    push(@tests, $testFile);
  }

  my %importsHash = map { $_ => 1} @imports;
  delete %importsHash{""};
  my $importsToReplace = join("\n", keys %importsHash);
  chomp $importsToReplace;

  my %extensionsHash = map { $_ => 1} @extensions;
  delete %extensionsHash{""};
  my $extensionsToReplace = join(" with ", keys %extensionsHash);
  chomp $extensionsToReplace;

  my $testsToReplace = join("\n", @tests);
  chomp $testsToReplace;

  if ($importsToReplace ne "") {
    $importsToReplace = "\n" . $importsToReplace;
  }

  if ($extensionsToReplace ne "") {
    $extensionsToReplace = " extends " . $extensionsToReplace;
  }

  if ($testsToReplace ne "") {
    $testsToReplace = "\n\n" . $testsToReplace;
  }

  $formProviderSpecTemplate =~ s/\$pre_formimports\$/$importsToReplace/;
  $formProviderSpecTemplate =~ s/\$pre_formextensions\$/$extensionsToReplace/;
  $formProviderSpecTemplate =~ s/\$pre_formFieldTests\$/$testsToReplace/;

  writeFile("$g8Directory/$templateName/generated-test/forms/\$section\$/\$className\$FormProviderSpec.scala", $formProviderSpecTemplate) or die "Couldn't write forms spec g8 template";
}

sub createViewTemplate($$) {
  my $templateName = shift;
  my $components = shift;

  mkdir("$g8Directory/$templateName/app/views", 0755) or die "Couldn't create views directory";
  mkdir("$g8Directory/$templateName/app/views/\$section\$", 0755) or die "Couldn't create views' section directory";
  
  my $viewTemplate = readFileToString("$generatorDirectory/view_template.scala.html");

  my @imports = ();
  my @parameters = ();
  my @components = ();

  my $index = 0;

  foreach (@$components) {
    my $component = $_;

    $index = $index + 1;

    my $importFile = readFileToString("$generatorDirectory/$component/view_import.scala");
    chomp $importFile;

    my $parameterFile = readFileToString("$generatorDirectory/$component/view_componentparameters.scala");
    chomp $parameterFile;

    my $componentFile = readFileToString("$generatorDirectory/$component/view_component.scala");
 
    my $fieldName = "\$field" . $index . "Name\$";

    $componentFile =~ s/\$pre_fieldname\$/$fieldName/g;
    $componentFile =~ s/\$pre_fieldindex\$/$index/g;

    push(@imports, $importFile);
    push(@parameters, $parameterFile);
    push(@components, $componentFile);
  }

  my %importsHash = map { $_ => 1} @imports;
  delete %importsHash{""};
  my $importsToReplace = join("\n", keys %importsHash);

  my %parametersHash = map { $_ => 1} @parameters;
  my $parametersToReplace = join("\n", keys %parametersHash);

  my $componentsToReplace = join("\n", @components);
  chomp $componentsToReplace;

  if ($importsToReplace ne "") {
    $importsToReplace = "\n" . $importsToReplace;
  }

  if ($parametersToReplace ne "") {
    $parametersToReplace = "\n" . $parametersToReplace;
  }

  if ($componentsToReplace ne "") {
    $componentsToReplace = "\n\n" . $componentsToReplace;
  }

  $viewTemplate =~ s/\$pre_viewimports\$/$importsToReplace/;
  $viewTemplate =~ s/\$pre_viewcomponentparameters\$/$parametersToReplace/;
  $viewTemplate =~ s/\$pre_viewcomponents\$/$componentsToReplace/;

  writeFile("$g8Directory/$templateName/app/views/\$section\$/\$className\$View.scala.html", $viewTemplate) or die "Couldn't write view g8 template";
}

sub createMigrationScript($$) {
  my $templateName = shift;
  my $components = shift;

  mkdir("$g8Directory/$templateName/migrations", 0755) or die "Couldn't create migrations directory";
  
  my $migrationTemplate = readFileToString("$generatorDirectory/\$className__snake\$.template.sh");

  my @componentSubheadings = ();
  my @componentMessages = ();

  my $index = 0;

  foreach (@$components) {
    my $component = $_;

    $index = $index + 1;

    my $componentSubheadingsFile = readFileToString("$generatorDirectory/$component/componentsubheading");
    chomp $componentSubheadingsFile;

    my $componentMessagesFile = readFileToString("$generatorDirectory/$component/componentmessages");
    chomp $componentMessagesFile;

    my $subheading = "\$subheading" . $index . "\$";

    $componentSubheadingsFile =~ s/\$pre_index\$/$index/g;
    $componentSubheadingsFile =~ s/\$pre_subheading\$/$subheading/g;

    $componentMessagesFile =~ s/\$pre_index\$/$index/g;

    push(@componentSubheadings, $componentSubheadingsFile);
    push(@componentMessages, $componentMessagesFile);
  }

  my $componentSubheadingsToReplace = join("\n", @componentSubheadings);
  chomp $componentSubheadingsToReplace;

  if ($componentSubheadingsToReplace ne "") {
    $componentSubheadingsToReplace = "\n" . $componentSubheadingsToReplace;
  }

  my $componentMessagesToReplace = join("\n", @componentMessages);
  chomp $componentMessagesToReplace;

  if ($componentMessagesToReplace ne "") {
    $componentMessagesToReplace = "\n" . $componentMessagesToReplace;
  }

  $migrationTemplate =~ s/\$pre_componentsubheadings\$/$componentSubheadingsToReplace/;
  $migrationTemplate =~ s/\$pre_componentmessages\$/$componentMessagesToReplace/;

  writeFile("$g8Directory/$templateName/migrations/\$className__snake\$.sh", $migrationTemplate) or die "Couldn't write migration script g8 template";
}

sub createProperties($@) {
  my $templateName = shift;
  my $components = shift;

  my $properties = << "END_PROPERTIES";
description = Generates a screen using the $templateName template
className = className
section = sectionName
sectionid = section-name
sectionmessage = section.sectionheadingmessage
navigator = navigator
url = url
title = page title
continueButtonName = saveAndContinue
END_PROPERTIES

  my @componentProperties = ();

  my $index = 0;

  foreach (@$components) {
    my $component = $_;

    $index = $index + 1;

    my $componentPropertiesFile = readFileToString("$generatorDirectory/$component/componentproperties");
    chomp $componentPropertiesFile;

    $componentPropertiesFile =~ s/\$pre_index\$/$index/g;

    push(@componentProperties, $componentPropertiesFile);
  }

  my $allComponentProperties = join("\n", @componentProperties);
  chomp $allComponentProperties;

  $properties = $properties . $allComponentProperties;

  writeFile("$g8Directory/$templateName/default.properties", $properties) or die "Couldn't write default.properties";
}

my @components = getComponentsFromUser();

my $templateName = getTemplateName();

mkdir("$g8Directory/$templateName", 0755) or die "Couldn't create base $templateName directory";
mkdir("$g8Directory/$templateName/app", 0755) or die "Couldn't create app directory";
mkdir("$g8Directory/$templateName/generated-test", 0755) or die "Couldn't create generated-test directory";

createControllerTemplate($templateName, \@components);
createFormProviderTemplate($templateName, \@components);
createModelClassTemplate($templateName, \@components);
createPageTemplate($templateName);
createCheckAnswersTemplate($templateName, \@components);
createViewTemplate($templateName, \@components);
createControllerSpecTemplate($templateName, \@components);
createFormProviderSpecTemplate($templateName, \@components);
createMigrationScript($templateName, \@components);
createProperties($templateName, \@components);

# messages
