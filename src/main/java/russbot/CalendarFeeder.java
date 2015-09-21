/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;

/**
 *
 * @author russfeld
 */
public class CalendarFeeder implements Runnable{
    Calendar calendar;
    
    public CalendarFeeder(String url) {
        InputStream stream = null;
        try {
            URL calurl = new URL(url);
            CalendarBuilder builder = new CalendarBuilder();
            stream = calurl.openStream();
            calendar = builder.build(stream);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CalendarFeeder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ParserException ex) {
            Logger.getLogger(CalendarFeeder.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
                Logger.getLogger(CalendarFeeder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        readCal();
    }
    
    public void readCal(){
        for (Iterator i = calendar.getComponents().iterator(); i.hasNext();) {
            Component component = (Component) i.next();
            System.out.println("Component [" + component.getName() + "]");

            for (Iterator j = component.getProperties().iterator(); j.hasNext();) {
                Property property = (Property) j.next();
                System.out.println("\tProperty [" + property.getName() + ", " + property.getValue() + "]");
            }
        }
    }
    
    
    public static void main (String[] args){
        String url = "https://orgsync.com/calendar/org/feed/3432595/44766b586c577c776c208b95b16cf32ff6e00484.ics?org=86744";
        new Thread(new CalendarFeeder(url)).start();
    }

    @Override
    public void run() {
        for (Iterator i = calendar.getComponents().iterator(); i.hasNext();) {
            Component component = (Component) i.next();
            System.out.println("Component [" + component.getName() + "]");

            for (Iterator j = component.getProperties().iterator(); j.hasNext();) {
                Property property = (Property) j.next();
                System.out.println("\tProperty [" + property.getName() + ", " + property.getValue() + "]");
            }
        }
    }
}
