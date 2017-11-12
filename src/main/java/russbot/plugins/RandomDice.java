/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot.plugins;

import russbot.Session;
import java.util.Random;
import java.util.regex.Matcher;

/**
 *
 * @author russfeld
 */
public class RandomDice implements Plugin {
    
    Random random;
    
    public RandomDice(){
        random = new Random();
    }

	/**
	 * @author hbgoddard
	 */
    @Override
    public String getRegexPattern() {
		/* KEY:
		 * 
		 * ^!r(oll)?			line starts with !r or !roll
		 * 
		 * [\d]*d[\d]+			standard dice format; <x>d<y> = roll x dice with y sides
		 * 						<x> is optional and defaults to 1 if omitted
		 * 
		 * ([d|k][l|h]?[\d]+)?	discard certain dice in the preceding roll:
		 * 						dl<z> = drop lowest z results
		 * 						dh<z> = drop highest z results
		 * 						kl<z> = keep lowest z results
		 * 						kh<z> = keep highest z results
		 * 						d<z> and k<z> are shorthand for dl<z> and kh<z>, respectively
		 * 						this group can be omitted entirely
		 * 
		 *  ?[+\-] ?			addition or subtraction (with optional spaces)
		 * 
		 * [\d]+				include a constant term instead of a roll
		 * 
		 */
		return "^!r(oll)? ([\\d]*d[\\d]+([d|k][l|h]?[\\d]+)?|[\\d]+)" +
			   "( ?[+\\-] ?([\\d]*d[\\d]+([d|k][l|h]?[\\d]+)?|[\\d]+))* *$";
		//don't you just love regular expressions?
    }

    @Override
    public String[] getChannels() {
        String[] channels = {"test"};
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
			"!r <x>d<y>                 - Roll x dice with y sides each",
			"!r <x>d<y>[d|k[l|h]<z>]    - Roll x dice with y sides and drop/keep the lowest/highest z rolls",
			"!r <set> [+|- <set> [...]] - Add or subtract multiple sets of dice or constants"
		};
        return commands;
    }

	/**
	 * @author hbgoddard
	 */
    @Override
    public void messagePosted(String message, String channel) {
		
		Pattern p = Pattern.compile("(([\\d]*d[\\d]+([d|k][l|h]?[\\d]+)*)|([+\\-])|([\\d]+)){1}");
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
					if (retVal == 0) return;
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
			catch (NumberFormatException nfe) {
				Session.getInstance().sendMessage("How did you get that weird number past my _glorious_ regex?! :rage:", channel);
				return;
			}
		}
		
		//append total and send message back to Slack
		output.append("= ").append(total);
		System.out.println(output.toString());
    }
	
	/**
	 * @author hbgoddard
	 */
	public int rollDice(String pattern, StringBuilder sb, String channel) {
		
		int numDice, numSides, toDrop, result = 0;
		boolean dropLowest = true;
		
		//get number of dice to roll
		if (pattern.charAt(0) == 'd') {
			numDice = 1;
		}
		else {
			numDice = Integer.parseInt(pattern.substring(0, pattern.indexOf('d')));
			if (numDice > 20 || numDice <= 0) {
				Session.getInstance().sendMessage("I don't have that many dice!", channel);
				return 0;
			}
		}
		
		//remove portion <x>d to simplify further processing
		pattern = pattern.substring(pattern.indexOf('d') + 1);
		
		//get size of the die and number of drops (if any)
		if (pattern.contains("d")) {
			numSides = Integer.parseInt(pattern.substring(0, pattern.indexOf('d')));
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
			numSides = Integer.parseInt(pattern.substring(0, pattern.indexOf('k')));
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
			numSides = Integer.parseInt(pattern);
			toDrop = 0;
		}
		
		//make sure numSides is reasonable
		if (numSides > 100000000 || numSides <= 0) {
			Session.getInstance().sendMessage("I don't have any dice of that size!", channel);
			return 0;
		}
		
		//make sure toDrop is reasonable
		if (toDrop < 0) toDrop = 0;
		if (toDrop > numDice) toDrop = numDice;
		
		//handle most common case with less overhead
		if (numDice == 1 && toDrop == 0) {
			result = random.nextInt(numSides) + 1;
			if (result == 1 || result == numSides) sb.append("*");
			sb.append("`").append(result).append("`");
			if (result == 1 || result == numSides) sb.append("*");
			sb.append(" ");
			return result;
		}
		
		//roll the dice!
		int[] rolls = new int[numDice];
		for (int i = 0; i < numDice; i++) {
			rolls[i] = random.nextInt(numSides) + 1;
		}
		
		//find which dice to drop
		boolean[] drop = new boolean[numDice];
		int dropVal, dropIndex;
		for (int j = 0; j < toDrop; j++) {
			dropVal = dropLowest ? Integer.MAX_VALUE : Integer.MIN_VALUE;
			dropIndex = -1;
			for (int i = 0; i < numDice; i++) {
				if (drop[i]) continue;
				if ((dropLowest && (rolls[i] < dropVal)) ||
					(!dropLowest && (rolls[i] > dropVal))) {
					dropVal = rolls[i];
					dropIndex = i;
				}
			}
			drop[dropIndex] = true;
		}
		
		//format output
		if (numDice > 1) sb.append("(");
		for (int i = 0; i < numDice; i++) {
			//strikethrough for dropped die
			if (drop[i]) sb.append("~");
			//if not dropped, add to sum
			else result += rolls[i];
			//bold for min or max value
			if (rolls[i] == 1 || rolls[i] == numSides) sb.append("*");
			sb.append("`").append(rolls[i]).append("`");
			if (rolls[i] == 1 || rolls[i] == numSides) sb.append("*");
			if (drop[i]) sb.append("~");
			if (i < numDice - 1) sb.append(" + ");
		}
		if (numDice > 1) sb.append(") ");
		else sb.append(" ");
		
		return result;
	}
}
