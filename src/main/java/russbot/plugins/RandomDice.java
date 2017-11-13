/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot.plugins;

import russbot.Session;
import java.util.Random;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 *
 * @author russfeld
 */
public class RandomDice extends Plugin {
	
	Random random;
	private static final int MAX_DICE = 20;
	private static final int MAX_DIE_SIZE = 1000;
	private static final int MAX_EXPLOSIONS = 100;
	
	public RandomDice(){
		random = new Random();
	}
	
	/**
	 * @author hbgoddard
	 */
	@Override
	public String getRegexPattern() {
		/**
		 * KEY:
		 * 
		 * ^!r(oll)?
		 * 		line starts with !r or !roll
		 * 
		 * [\d]*d[\d]+
		 * 		standard dice format; <x>d<y> = roll x dice with y sides
		 * 		<x> is optional and defaults to 1 if omitted
		 * 
		 * [\d]+
		 * 		include a constant term instead of a roll
		 * 
		 * (!!?(([<|>][\d]+)|([\d]*)))?
		 * 		explode certain dice in the preceding roll;
		 * 		<x>d<y>!<z>  = explode; roll an additional die any time z is rolled in the preceding set
		 * 		<x>d<y>!!<z> = compound; same as explode, but add additional rolls to the triggering roll
		 * 		if <z> is omitted, default value is y
		 * 		if <z> is preceded by > or <, the range for the triggering roll is extended to be >= z or <= z, respectively
		 * 		if a comparator is present, <z> cannot be omitted
		 * 
		 * ([d|k][l|h]?[\d]+)?
		 * 		drop or keep certain dice in the preceding roll:
		 * 		dl<z> = drop lowest z results
		 * 		dh<z> = drop highest z results
		 * 		kl<z> = keep lowest z results
		 * 		kh<z> = keep highest z results
		 * 		d<z> and k<z> are shorthand for dl<z> and kh<z>, respectively
		 * 
		 *  ?[+\-] ?
		 * 		addition or subtraction (with optional spaces)
		 * 
		 */
		return "^!r(oll)? (([\\d]*d[\\d]+(!!?(([<|>][\\d]+)|([\\d]*)))?([d|k][l|h]?[\\d]+)?)|([\\d]+))" +
		       "( ?[+\\-] ?(([\\d]*d[\\d]+(!!?(([<|>][\\d]+)|([\\d]*)))?([d|k][l|h]?[\\d]+)?)|([\\d]+)))*\\s*$";
		//don't you just love regular expressions?
	}
	
	@Override
	public String[] getChannels() {
		String[] channels = {"test", "random"};
		return channels;
	}
	
	@Override
	public String getInfo(){
		return "for rolling random integers like dice";
	}
    
	/**
	 * @author hbgoddard
	 */
	@Override
	public String[] getCommands(){
		String[] commands = {
			"`!r <x>d<y>                ` - Roll `x` dice with `y` sides each",
			"`!r d<y>                   ` - If `<x>` is omitted, default value is 1",
			"`!r <x>d<y>d|k[l|h]<z>     ` - Roll `x` dice with `y` sides and `d`rop/`k`eep the `l`owest/`h`ighest `z` rolls",
			"`!r <x>d<y>d|k<z>          ` - A `d` or `k` by itself will default to drop lowest and keep highest, respectively",
			"`!r <x>d<y>!<z>            ` - Explode: Roll `x` dice with `y` sides, rolling again each time `z` is rolled (including rerolls)",
			"`!r <x>d<y>!               ` - If `<z>` is omitted, then by default dice will explode on their highest value",
			"`!r <x>d<y>![[<|>]<z>]     ` - Explode on rolls less than or equal to `z` or greater than or equal to `z`",
			"`!r <x>d<y>[!<z>][d|k<w>]  ` - Explode and drop/keep may be combined, but drop/keep must come last",
			"`!r <x>d<y>!![[<|>]<z>]    ` - Compounding explode: same as explode, but adds rerolls to the original roll",
			"`!r <set> [+|- <set> [...]]` - Add or subtract multiple sets of dice or constants"
		};
		return commands;
	}

	/**
	 * @author hbgoddard
	 */
	@Override
	public void messagePosted(String message, String channel) {
		//fix HTML-ified characters
		message = message.replaceAll("&lt", "<");
		message = message.replaceAll("&gt", ">");
		
		//                             |<- roll  ->||<-------- explode  -------->||<-- drop/keep  -->|
		Pattern p = Pattern.compile("(([\\d]*d[\\d]+(!!?(([<|>][\\d]+)|([\\d]*)))?([d|k][l|h]?[\\d]+)?)|([+\\-])|([\\d]+)){1}");
		Matcher m = p.matcher(message);
		StringBuilder output = new StringBuilder("");
		
		int total = 0;
		boolean add = true;
		String token;
		while (m.find()) {
			try {
				//get input fragment
				token = message.substring(m.start(), m.end());
				//if it is a dice fragment, send it to the roller
				if (token.contains("d")) {
					int retVal = rollDice(token, output, channel);
					if (retVal == -1) return;
					else total += add ? retVal : -retVal;
				}
				//if it is an operation, update the add flag
				else if (token.charAt(0) == '+') {
					add = true;
					output.append("+ ");
				}
				else if (token.charAt(0) == '-') {
					add = false;
					output.append("- ");
				}
				//otherwise it is a constant, so just add/subtract it
				else {
					int num = Integer.parseInt(token);
					total += add ? num : -num;
					output.append(num).append(" ");
				}
			}
			catch (NumberFormatException e) {
				Session.getInstance().sendMessage("/shrug I'm not quite sure how to interpret one of those numbers... ", channel);
				return;
			}
			catch (Exception e) {
				Session.getInstance().sendMessage("How did you get that past my _glorious_ regex?! :rage:\n" +
												  "(hey @hbgoddard, something's broken)", channel);
				return;
			}
		}
		
		//append total and send message back to Slack
		output.append("= ").append(total);
		Session.getInstance().sendMessage(output.toString(), channel);
	}
	
	/**
	 * @author hbgoddard
	 */
	private int rollDice(String pattern, StringBuilder sb, String channel) {
		
		int numDice, numSides, result = 0;
		int end;
		
		//get number of dice to roll
		if (pattern.charAt(0) == 'd') {
			numDice = 1;
		}
		else {
			numDice = Integer.parseInt(pattern.substring(0, pattern.indexOf('d')));
			//make sure number of dice is reasonable
			if (numDice > MAX_DICE || numDice <= 0) {
				Session.getInstance().sendMessage("I can't roll " + numDice + " dice!", channel);
				return -1;
			}
		}
		
		//remove fragment <x>d to simplify further processing
		pattern = pattern.substring(pattern.indexOf('d') + 1);
		
		//get size of the die
		if (pattern.contains("!")) end = pattern.indexOf('!');
		else if (pattern.contains("d")) end = pattern.indexOf('d');
		else if (pattern.contains("k")) end = pattern.indexOf('k');
		else end = pattern.length();
		numSides = Integer.parseInt(pattern.substring(0, end));
		//remove fragment <y> to simplify further processing
		pattern = pattern.substring(end);
		
		//make sure numSides is reasonable
		if (numSides > MAX_DIE_SIZE || numSides <= 0) {
			Session.getInstance().sendMessage("I don't have any dice with " + numSides + " sides!", channel);
			return -1;
		}
		
		//roll the dice!
		ArrayList<Integer> rolls = new ArrayList<Integer>(numDice);
		for (int i = 0; i < numDice; i++) {
			rolls.add(random.nextInt(numSides) + 1);
		}
		
		//explode dice
		ArrayList<Boolean> exploded = new ArrayList<Boolean>(numDice);
		for (int i = 0; i < numDice; i++) {
			exploded.add(false);
		}
		if (pattern.contains("!")) {
			//find explosion values
			int expVal, expGTE, expLTE;
			boolean compound = false;
			int start;
			//get end index of value in pattern
			if (pattern.contains("d")) end = pattern.indexOf('d');
			else if (pattern.contains("k")) end = pattern.indexOf('k');
			else end = pattern.length();
			//get start index of value in pattern
			start = 1;
			//check if explosion is compounding
			if (pattern.contains("!!")) {
				start++;
				compound = true;
			}
			//check for comparator
			if (pattern.contains("<")) {
				expVal = Integer.parseInt(pattern.substring(start + 1, end));
				expGTE = 1;
				expLTE = expVal;
			}
			else if (pattern.contains(">")) {
				expVal = Integer.parseInt(pattern.substring(start + 1, end));
				expGTE = expVal;
				expLTE = numSides;
			}
			//if value is omitted, default to max roll on die
			else if (start == end) {
				expGTE = numSides;
				expLTE = numSides;
			}
			//if value is present with no comparator, explode only on value
			else {
				expVal = Integer.parseInt(pattern.substring(start, end));
				expGTE = expVal;
				expLTE = expVal;
			}
			//remove explosion fragment to simplify further processing
			pattern = pattern.substring(end);
			
			//make sure bounds are reasonable
			if (expGTE <= 1 && expLTE >= numSides) {
				Session.getInstance().sendMessage("Too many explosions!", channel);
				return -1;
			}
			
			//finally we get to blow things up
			int newRoll;
			int explodeCount;
			int newDice = 0;
			for (int i = 0; i < numDice; i++) {
				//check if this is an exploding value
				if (rolls.get(i) >= expGTE && rolls.get(i) <= expLTE) {
					if (compound) {
						exploded.set(i, true);
						explodeCount = 0;
						do {
							newRoll = random.nextInt(numSides) + 1;
							rolls.set(i, rolls.get(i) + newRoll);
							explodeCount++;
							if (explodeCount > MAX_EXPLOSIONS) {
								Session.getInstance().sendMessage("Too many explosions!", channel);
								return -1;
							}
						} while (newRoll >= expGTE && newRoll <= expLTE);
					}
					else {
						rolls.add(i + 1, random.nextInt(numSides) + 1);
						exploded.add(i + 1, true);
						numDice++;
						newDice++;
					}
				}
				if (newDice > MAX_EXPLOSIONS) {
					Session.getInstance().sendMessage("Too many explosions!", channel);
					return -1;
				}
			}
		}
		
		//get number of drops (if any)
		int toDrop = 0;
		boolean dropLowest = true;
		if (pattern.contains("d")) {
			//drop lowest
			if (pattern.contains("l")) {
				dropLowest = true;
				toDrop = Integer.parseInt(pattern.substring(pattern.indexOf('l') + 1));
			}
			//drop highest
			else if (pattern.contains("h")) {
				dropLowest = false;
				toDrop = Integer.parseInt(pattern.substring(pattern.indexOf('h') + 1));
			}
			//drop lowest (d = dl)
			else {
				dropLowest = true;
				toDrop = Integer.parseInt(pattern.substring(pattern.indexOf('d') + 1));
			}
		}
		else if (pattern.contains("k")) {
			//keep lowest
			if (pattern.contains("l")) {
				dropLowest = false;
				toDrop = Integer.parseInt(pattern.substring(pattern.indexOf('l') + 1));
			}
			//keep highest
			else if (pattern.contains("h")) {
				dropLowest = true;
				toDrop = Integer.parseInt(pattern.substring(pattern.indexOf('h') + 1));
			}
			//keep highest (k = kh)
			else {
				dropLowest = true;
				toDrop = Integer.parseInt(pattern.substring(pattern.indexOf('k') + 1));
			}
			//keep z out of x dice = drop (x-z) out of x dice
			toDrop = numDice - toDrop;
		}
		else {
			toDrop = 0;
		}
		
		//make sure toDrop is reasonable
		if (toDrop < 0) toDrop = 0;
		if (toDrop > numDice) toDrop = numDice;
		
		//find which dice to drop
		boolean[] drop = new boolean[numDice];
		int dropVal, dropIndex;
		for (int j = 0; j < toDrop; j++) {
			dropVal = dropLowest ? Integer.MAX_VALUE : Integer.MIN_VALUE;
			dropIndex = -1;
			for (int i = 0; i < numDice; i++) {
				//skip if already dropped
				if (drop[i]) continue;
				if ((dropLowest && (rolls.get(i) < dropVal)) ||
					(!dropLowest && (rolls.get(i) > dropVal))) {
					dropVal = rolls.get(i);
					dropIndex = i;
				}
			}
			//mark min/max value as dropped
			drop[dropIndex] = true;
		}
		
		//format output
		if (numDice > 1) sb.append("(");
		for (int i = 0; i < numDice; i++) {
			//strikethrough for dropped die
			if (drop[i]) sb.append("~");
			//if not dropped, add to sum
			else result += rolls.get(i);
			//bold for min or max value
			if (rolls.get(i) == 1 || rolls.get(i) == numSides) sb.append("*");
			//italicize for exploded value
			if (exploded.get(i)) sb.append("_");
			sb.append("`").append(rolls.get(i)).append("`");
			//close italics
			if (exploded.get(i)) sb.append("_");
			//close bold
			if (rolls.get(i) == 1 || rolls.get(i) == numSides) sb.append("*");
			//close strikethrough
			if (drop[i]) sb.append("~");
			if (i < numDice - 1) sb.append(" + ");
		}
		if (numDice > 1) sb.append(") ");
		else sb.append(" ");
		
		return result;
	}
}
