require 'digest/sha2'
require 'fileutils'

similarities = {}
files = ARGV
suffix = File.extname(ARGV[0] || "")
$stderr.puts "Found #{files.size} entries matching the pattern"
files.each do |path|
  if File.file?(path)
    File.open(path) do |file|
      digest = Digest::SHA2.hexdigest(file.read)
      if similarities.has_key?(digest)
        similarities[digest] << path
      else
        similarities[digest] = [path]
      end
    end
  end
end
similarities.each do |_, files|
  if files.size > 1
    merged = files.map{|f| File.basename(f, suffix)}.sort.join("_") + suffix
    $stderr.puts "Merging to #{merged}"
    
    # Rename first file
    File.rename(files[0], File.join(File.dirname(files[0]), merged))
    
    # Remove duplicates
    FileUtils.rm(files.drop(1))
  end
end

$stderr.puts "Similar files have now been merged. Run the updatemelodies script to create an updated hymnbook"
