
if ARGV.size > 0
  melodies = {}
  files = ARGV
  $stderr.puts "Found #{files.size} files matching the pattern."
  files.each do |f|
    f = File.basename(f)
    names = f.gsub(/\.[a-z]+$/, '').split("_")
    names.each do |name|
      if name =~ /^(\d+)([a-z]*)$/i
        num, id = $1, $2
        num.gsub!(/^0+/, '')
        id = id.empty? ? "A" : id.capitalize
        melody = "<melody><id>#{id}</id><file>#{f}</file><author/><sheet/></melody>"
        if melodies.has_key?(num)
          melodies[num] << melody
        else
          melodies[num] = [melody]
        end
      end
    end
  end
  $stderr.puts "Identified melodies for #{melodies.size} hymns."
  melodies.each do |num, ms|
    $stdout.puts "#{num}-#{num} : melodies=<melodies>#{ms.sort.join}</melodies>"
  end
else
  $stderr.puts "Please specify a target pattern. E.g. *.ogg"
end
