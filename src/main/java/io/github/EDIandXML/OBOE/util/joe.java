package io.github.EDIandXML.OBOE.util;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class joe {

	public static void main(String[] args) {
		TreeSet<String> set = new TreeSet<String>();
		Path file = Paths.get(
				"C:\\Users\\joe\\git\\OBOEOpenSource\\OBOE\\xml\\orders.xml");
		try (Stream<String> lines = Files.lines(file)) {
			lines.forEach(line -> {
				if (line.contains("<idListFile fileName=\"")) {
					Pattern p = Pattern.compile("\"([^\"]*)\"");
					Matcher m = p.matcher(line);
					while (m.find()) {

						var found = m.group(1);

						var pt = Paths.get(
								"C:\\Users\\joe\\git\\OBOEOpenSource\\OBOE\\originalxml\\"
										+ found);
						System.out.println(pt.toString());
						if (Files.exists(pt) == true) {
							try {
								if (set.contains(found) == false) {
									Files.copy(pt,
											FileSystems.getDefault().getPath(
													"C:\\Users\\joe\\git\\OBOEOpenSource\\OBOE\\xml\\",
													found),
											StandardCopyOption.REPLACE_EXISTING);
								}
								set.add(found);
								System.out.println("moved " + pt.getFileName());

							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
