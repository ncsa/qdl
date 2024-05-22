# This takes two arguments --
# 1 = the root docs directory of the install where the documents are found
# 2 = the target directory for the output.

# remember that this uses C-style array for the args, so ${args[0]} is the 1st arg
# not the program name!

args=("$@")
cd ${args[1]} || exit

echo "converting QDL docs to PDF"

lowriter --headless --convert-to pdf ${args[0]}/iso6429.odt              > /dev/null
lowriter --headless --convert-to pdf ${args[0]}/qdl_configuration.odt    > /dev/null
lowriter --headless --convert-to pdf ${args[0]}/qdl_extensions.odt       > /dev/null
lowriter --headless --convert-to pdf ${args[0]}/qdl_ini_file.odt         > /dev/null
lowriter --headless --convert-to pdf ${args[0]}/qdl_reference.odt        > /dev/null
lowriter --headless --convert-to pdf ${args[0]}/qdl_scripting.odt        > /dev/null
lowriter --headless --convert-to pdf ${args[0]}/qdl_server_scripts.odt   > /dev/null
lowriter --headless --convert-to pdf ${args[0]}/old-module-reference.odt > /dev/null
lowriter --headless --convert-to pdf ${args[0]}/module-reference.odt     > /dev/null
lowriter --headless --convert-to pdf ${args[0]}/qdl_swing_gui.odt        > /dev/null
lowriter --headless --convert-to pdf ${args[0]}/qdl_workspace.odt        > /dev/null
lowriter --headless --convert-to pdf ${args[0]}/tutorial.odt             > /dev/null
lowriter --headless --convert-to pdf ${args[0]}/qdldb-extension.odt      > /dev/null
lowriter --headless --convert-to pdf ${args[0]}/http-extension.odt       > /dev/null
lowriter --headless --convert-to pdf ${args[0]}/xml-extension.odt        > /dev/null
lowriter --headless --convert-to pdf ${args[0]}/cli-extension.odt        > /dev/null
lowriter --headless --convert-to pdf ${args[0]}/crypto.odt               > /dev/null
lowriter --headless --convert-to pdf ${args[0]}/qdl_mail.odt             > /dev/null
echo "   ... done with QDL docs"