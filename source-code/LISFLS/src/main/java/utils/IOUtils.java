package utils;

import java.io.*;

public class IOUtils {
    /**
     * 将 content 写入到 file 中
     * @param content 要写入的内容
     * @param file 要写入的文件路径
     * @throws IOException
     */
    public static void fileWrite(String content, String file) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        OutputStreamWriter bufout = new OutputStreamWriter(out);
        BufferedWriter bufferedwriter = new BufferedWriter(bufout, content.getBytes().length);
        bufferedwriter.write(content);
        bufferedwriter.close();
        bufout.close();
        out.close();
    }

    /**
     * 调用终端命令 cmd，并且通过标准输入写入 content
     * @param cmd 终端调用命令
     * @param content 终端命令的标准输入, 若没有则写 null
     * @return 新的线程
     * @throws IOException
     */
    public static Process invoke(String cmd, String content) throws IOException {
        Process p = Runtime.getRuntime().exec(cmd); //执行命令

        if (content != null) {
            OutputStream o = p.getOutputStream();
            OutputStreamWriter ou = new OutputStreamWriter(o);
            BufferedWriter OUT = new BufferedWriter(ou);
//            System.out.println(" == "+content);
            OUT.write(content);
            OUT.close();
            ou.close();
            o.close();
        }

        return p;
    }

    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); // 删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            java.io.File myFilePath = new java.io.File(filePath);
            myFilePath.delete(); // 删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 删除指定文件夹下所有文件
    // param path 文件夹完整绝对路径
    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);// 再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }
}
