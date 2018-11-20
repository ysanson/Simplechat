// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package common;

import java.util.List;

/**
 * This interface implements the abstract method used to display
 * objects onto the client or server UIs.
 *
 * @author Dr Robert Lagani&egrave;re
 * @author Dr Timothy C. Lethbridge
 * @version July 2000
 */
public interface ChatIF 
{
    /**
    * Method that when overriden is used to display objects onto
    * a UI.
    */
    void display(String message);

    /**
     * Method that when overriden is used to update the client list onto a UI.
     */
    void updateClientList(List<String> clients);
}
