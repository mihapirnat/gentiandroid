/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.formias.gentian.http;

/** Observer to the process of download/upload.
 *
 * @author miha
 */
public interface ProgressObserver {

    /**Transfer direction: download/upload
     *
     */
    public static enum Direction {

        /** download*/
        Download,/** upload*/
        Upload;
    }

    /** Reports details of file transfer in progress
     * @param direction direction of this transfer
     * @param file  Filename
    @param position Offset from start of file
    @param length Total length of file

     * @param timestamp unix timestamp of last modification
     */
    public void progress(Direction direction, String file, long position, long length, long timestamp);
}
