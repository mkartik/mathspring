package edu.umass.ckc.wo.ttmain.ttservice.loginservice.impl;

import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Random;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import com.sun.mail.smtp.SMTPTransport;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

import edu.umass.ckc.wo.beans.Teacher;
import edu.umass.ckc.wo.beans.ClassInfo;
import edu.umass.ckc.wo.beans.Classes;
import edu.umass.ckc.wo.cache.ProblemMgr;
import edu.umass.ckc.wo.db.DbClass;
import edu.umass.ckc.wo.db.DbPedagogy;
import edu.umass.ckc.wo.db.DbTeacher;
import edu.umass.ckc.wo.smgr.User;
import edu.umass.ckc.wo.ttmain.ttconfiguration.TTConfiguration;
import edu.umass.ckc.wo.ttmain.ttconfiguration.errorCodes.ErrorCodeMessageConstants;
import edu.umass.ckc.wo.ttmain.ttconfiguration.errorCodes.TTCustomException;
import edu.umass.ckc.wo.ttmain.ttmodel.CreateClassForm;
import edu.umass.ckc.wo.ttmain.ttservice.loginservice.TTLoginService;
import edu.umass.ckc.wo.tutor.Settings;

/**
 * Created by Neeraj on 3/25/2017.
 * Frank 	02-24-20	Issue #28
 * Frank	04-27-2020  Disable password update until email is working
 * Frank    05-12-2020  send password in email now working 
 */
@Service
public class TTLoginServiceImpl implements TTLoginService {
    private static Logger logger = Logger.getLogger(TTLoginServiceImpl.class);

    @Autowired
    private TTConfiguration connection;

    @Override
    public int loginAssist(String uname, String password, String forgotPassword) throws TTCustomException {
        try {
            int teacherId = DbTeacher.getTeacherId(connection.getConnection(), uname, password);
            if (teacherId == -1) {
            	if (forgotPassword.equals("on")) {
                    Teacher teacher = DbTeacher.getTeacherFromUsername(connection.getConnection(), uname);
                    Random rand = new Random();
                    int x = rand.nextInt(99999);
                    String symbols = "!#$%";
                    int mod = x % 3;
                    String pw = uname.substring(0,2) + Integer.toString(x) + symbols.substring(mod,mod+1);
                    logger.info(uname + ":" + pw);
                    System.out.println(uname + ":" + pw);

                    DbTeacher.modifyTeacherPassword(connection.getConnection(),uname,pw);
                   
                    Properties prop = System.getProperties();
                    prop.put("mail.smtp.host", "mailsrv.cs.umass.edu"); //optional, defined in SMTPTransport
                    prop.put("mail.smtp.auth", "true");
                    prop.put("mail.smtp.port", "25"); // default port 25
                    
                    Session session = Session.getInstance(prop, null);
                    Message msg = new MimeMessage(session);
                    try {

            			// from
                        msg.setFrom(new InternetAddress("mathspring@cs.umass.edu"));

            			// to
                        msg.setRecipients(Message.RecipientType.TO,
                                InternetAddress.parse(teacher.getEmail(), false));

            			// subject
                        msg.setSubject("Your replacement Mathspring password");

            			// content
                        msg.setText(pw);

                        msg.setSentDate(new Date());

            			// Get SMTPTransport
                        SMTPTransport t = (SMTPTransport) session.getTransport("smtps");

            			// connect
                        t.connect("mailsrv.cs.umass.edu", "mathspring@cs.umass.edu", "m4thspr1ng!");

                        // send
                        t.sendMessage(msg, msg.getAllRecipients());

                        //System.out.println("Response: " + t.getLastServerResponse());

                        t.close();

                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }

            	}
                return -1;
            }
            else
                return teacherId;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            throw new TTCustomException(ErrorCodeMessageConstants.ERROR_OCCURED_ON_AUTHENTICATE);
        }

    }

    @Override
    public String populateClassInfoForTeacher(ModelMap model, int teacherId) throws TTCustomException {
        try {

            if (!ProblemMgr.isLoaded()) {
                ProblemMgr.loadProbs(connection.getConnection());
                Settings.lessonMap = DbPedagogy.buildAllLessons(connection.getConnection());
                Settings.loginMap = DbPedagogy.buildAllLoginSequences(connection.getConnection());
                Settings.pedagogyGroups = DbPedagogy.buildAllPedagogies(connection.getConnection(), null);
            }
            ClassInfo[] classes = DbClass.getClasses(connection.getConnection(), teacherId);
            List<ClassInfo> classInfoList = new  ArrayList<>(Arrays.asList(classes));
            Map<Boolean, List<ClassInfo>> groups = classInfoList.stream().collect(Collectors.partitioningBy(c -> (c.getSchoolYear() == Year.now().getValue())));
            List<ClassInfo> classInfoListLatest = groups.get(true);
            List<ClassInfo> classInfoListArchived = groups.get(false);
            Collections.reverse(classInfoListLatest);
            Classes classbean = new Classes(classInfoListLatest.toArray(new ClassInfo[classInfoListLatest.size()]));
            Classes classbeanArchived = new Classes(classInfoListArchived.toArray(new ClassInfo[classInfoListArchived.size()]));
            //String teacherName = DbTeacher.getTeacherName(connection.getConnection(), teacherId);
            Teacher teacher = DbTeacher.getTeacher(connection.getConnection(), teacherId);
            model.addAttribute("teacherName", teacher.getFname() + " " + teacher.getLname());
            model.addAttribute("teacherFname", teacher.getFname());
            model.addAttribute("teacherLname", teacher.getLname());
            model.addAttribute("teacherEmail", teacher.getEmail());
            model.addAttribute("teacherId", Integer.toString(teacherId));
            model.addAttribute("createClassForm", new CreateClassForm());
            
            if (classes.length > 0) {
                int classId = classInfoList.get(0).getClassid();
                ClassInfo classInfo = DbClass.getClass(connection.getConnection(), classId);
                List<User> students = DbClass.getClassStudents(connection.getConnection(), classId);
                model.addAttribute("students", students);
                model.addAttribute("classbean", classbean);
                model.addAttribute("classbeanArchived", classbeanArchived);
                model.addAttribute("classInfo", classInfo);
                model.addAttribute("noClass", false);
                model.addAttribute("classList", classes);
                return "teacherTools/teacherToolsMain";
            } else {
                model.addAttribute("noClass", true);
                return "teacherTools/teacherToolsMain";
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            throw new TTCustomException(ErrorCodeMessageConstants.ERROR_OCCURED_ON_LOADING_TEACHER_DATA);
        }
    }


}
