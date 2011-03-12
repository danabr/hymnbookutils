
if ARGV.size > 0
  melodies = {}
  files = ARGV
  $stderr.puts "Found #{files.size} files matching the pattern."
  files.each do |f|
    f = File.basename(f)
    if f =~ /^(\d+)([a-z]*)\.[a-z]+$/i
      num, name = $1, $2
      num.gsub!(/^0+/, '')
      name = name.empty? ? "A" : name.capitalize
      melody = "<melody><id>#{name}</id><file>#{f}</file><author/><sheet/></melody>"
      if melodies.has_key?(num)
        melodies[num] += melody
      else
        melodies[num] = melody
      end
    end
  end
  $stderr.puts "Identified melodies for #{melodies.size} hymns."
  melodies.each do |num, ms|
    $stdout.puts "#{num}-#{num} : melodies=<melodies>#{ms}</melodies>"
  end
else
  $stderr.puts "Please specify a target pattern. E.g. *.ogg"
end
