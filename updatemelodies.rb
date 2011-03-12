if ARGV.size == 3
  infile, outfile, melody_dir = ARGV[0], ARGV[1], ARGV[2]
  identifier_path = File.join(File.dirname(__FILE__),  "hymnidentifier", "identifier.rb")
  editor_path = File.join(File.dirname(__FILE__), "hymnbookeditor")
  [infile, melody_dir, identifier_path, editor_path + '\\HymnbookEditor.class'].each do |path|
    unless File.exists? path
      $stderr.puts "No such file or directory: #{path}"
      exit(-1) 
    end
  end   
  
  # Identify melodies and create command file
  $stderr.puts "Identifying melodies..."
  `ruby #{identifier_path} #{File.join(melody_dir, "*")} > commands.txt`
  # Update the hymnbook
  $stderr.puts "Creating updated hymnbook"
  `scala -cp #{editor_path} HymnbookEditor #{infile} #{outfile} < commands.txt`
  $stderr.puts "Your hymnbook has been updated"
else
  puts "Usage: ruby updatemelodies in.xml out.xml path\\to\\melodies"
end
