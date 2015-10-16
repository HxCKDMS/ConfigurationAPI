import Configuration.Category;
import Configuration.Config;
import Configuration.HxCConfig;

import java.io.File;
import java.util.LinkedHashMap;

public class Example {
	@Config.Map
	public static LinkedHashMap<String, String> StrStr = new LinkedHashMap<>();
	@Config.Map
	public static LinkedHashMap<String, Integer> StrInt = new LinkedHashMap<>();
	@Config.Map
	public static LinkedHashMap<String, Boolean> StrBol = new LinkedHashMap<>();
	@Config.Map
	public static LinkedHashMap<String, Double> StrDbl = new LinkedHashMap<>();
	@Config.Map
	public static LinkedHashMap<String, Character> StrChr = new LinkedHashMap<>();
	@Config.Map
	public static LinkedHashMap<String, Float> StrFlt = new LinkedHashMap<>();
	@Config.Map
	public static LinkedHashMap<String, Long> StrLng = new LinkedHashMap<>();
	
	static{
		StrStr.put("ExampleString", "Example String");
		StrInt.put("ExampleInt", 1);
		StrBol.put("ExampleBoolean", true);
		StrDbl.put("ExampleDouble", 1.0);
		StrChr.put("ExampleCharacter", 'e');
		StrFlt.put("ExampleFloat", 10.1111f);
		StrLng.put("ExampleLong", 9223372036850000000l);
	}

	public static void main(String[] args) {
		registerCFGS(new HxCConfig());

		StrStr.forEach((z, x) -> System.out.println("<String, String> " + z + "=" + x));
		StrInt.forEach((z, x) -> System.out.println("<String, Integer> " + z + "=" + x));
		StrBol.forEach((z, x) -> System.out.println("<String, Boolean> " + z + "=" + x));
		StrDbl.forEach((z, x) -> System.out.println("<String, Double> " + z + "=" + x));
		StrChr.forEach((z, x) -> System.out.println("<String, Character> " + z + "=" + x));
		StrFlt.forEach((z, x) -> System.out.println("<String, Float> " + z + "=" + x));
		StrLng.forEach((z, x) -> System.out.println("<String, Long> " + z + "=" + x));
		}

	public static void registerCFGS(HxCConfig cfg) {
		cfg.registerCategory(new Category("General"));
		cfg.handleConfig(Example.class, new File("ExampleCFG.cfg"));
	}
}