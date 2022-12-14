package com.lyd.service;

import com.aliyun.oss.OSSClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.lyd.config.OssPath;
import com.lyd.controller.VO.*;
import com.lyd.controller.dto.UserInfoDTO;
import com.lyd.controller.dto.UserDTO;
import com.lyd.controller.dto.UserRegDTO;
import com.lyd.entity.*;
import com.lyd.mapper.*;
import com.lyd.utils.OssUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetValuedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private UserLikeMapper userLikeMapper;
    @Resource
    private UserCollectionMapper userCollectionMapper;
    @Resource
    private PostCommentsMapper postCommentsMapper;
    @Resource
    private CommentCommentsMapper commentCommentsMapper;
    @Resource
    private UserReportMapper userReportMapper;
    @Autowired
    private OSSClient ossClient;
    @Autowired
    private OssPath ossPath;
    @Autowired
    private BCryptPasswordEncoder encoder;
    @Resource
    private PostsMapper postsMapper;
    @Resource
    private HistoryMapper historyMapper;
    @Resource
    private DocumentMapper documentMapper;
    @Resource
    private TodoMapper todoMapper;
    @Resource
    private MessageMapper messageMapper;
    @Resource
    private VideoMapper videoMapper;
    @Autowired
    private UserService userService;

    /**
     * @desc    ??????????????????
     * @return
     */
    public Long getUserCount() {
        return userMapper.selectCount(null);
    }

    /**
     * @desc    ??????
     * @param userRegDTO
     */
    public void register(UserRegDTO userRegDTO) {
        User user = new User();
        UserInfo userInfo = new UserInfo();

        long id = IdWorker.getId(user);
        user.setId(id);
        user.setEmail(userRegDTO.getEmail());
        user.setPassword(encoder.encode(userRegDTO.getPassword()));
        user.setRole("ROLE_USER");
        userInfo.setId(id);
//        userInfo.setNickname(id+"??????");

        userMapper.insert(user);
        userInfoMapper.insert(userInfo);
    }

    /**
     * @desc    ??????????????????
     * @param userDTO
     */
    public void updPwd(@RequestBody UserDTO userDTO) {
        String password = userDTO.getPassword();
        password = encoder.encode(password);
        User newPwdUser = getUserByEmail(userDTO.getUsername());
        newPwdUser.setPassword(password);
        userMapper.updateById(newPwdUser);
    }

    /**
     * @desc    ??????????????????
     * @param userInfoDTO
     */
    public void updUserInfo(UserInfoDTO userInfoDTO) {
        Long userId = Long.valueOf(userInfoDTO.getUserId());
        if (userInfoDTO.getEmail()!=null) {
            User user = new User();
            user.setId(userId);
            user.setEmail(userInfoDTO.getEmail());
            userMapper.updateById(user);
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setId(userId);
        userInfo.setNickname(userInfoDTO.getNickname());
        userInfo.setSignature(userInfoDTO.getSignature());
        if (userInfoDTO.getGender()!=null) {
            if (userInfoDTO.getGender()==0) {
                userInfo.setGender(false);
            } else if (userInfoDTO.getGender()==1) {
                userInfo.setGender(true);
            } else {
                userInfo.setGender(null);
            }
        }

        userInfo.setSchool(userInfoDTO.getSchool());
        userInfo.setAcademic(userInfoDTO.getAcademic());
        userInfo.setProfession(userInfoDTO.getProfession());
        userInfo.setGrade(userInfoDTO.getGrade());
        userInfoMapper.updateById(userInfo);
    }

    /**
     * @desc    ????????????
     * @param userId
     */
    public void banUser(Long userId) {
        userMapper.deleteById(userId);
        int user = userInfoMapper.deleteById(userId);
        log.info("????????????{}???user??????",user);
        QueryWrapper<Posts> postWrapper = new QueryWrapper<>();
        postWrapper.eq("user_id",userId);
        int post = postsMapper.delete(postWrapper);
        log.info("????????????{}???post??????",post);
        QueryWrapper<PostComments> pcWrapper = new QueryWrapper<>();
        pcWrapper.eq("user_id",userId);
        int pc = postCommentsMapper.deleteById(userId);
        log.info("????????????{}???pc??????",pc);
        QueryWrapper<CommentComments> ccWrapper = new QueryWrapper<>();
        ccWrapper.eq("from_id",userId);
        int cc = commentCommentsMapper.delete(ccWrapper);
        log.info("????????????{}???cc??????",cc);
        QueryWrapper<Video> videoWrapper = new QueryWrapper<>();
        videoWrapper.eq("user_id",userId);
        int video = videoMapper.delete(videoWrapper);
        log.info("????????????{}???video??????",video);
        QueryWrapper<Document> docWrapper = new QueryWrapper<>();
        docWrapper.eq("publisher_id",userId);
        int doc = documentMapper.delete(docWrapper);
        log.info("????????????{}???doc??????",doc);
    }

    /**
     * @desc    ??????????????????
     * @param userId
     */
    public void unbanUser(Long userId) {
        userMapper.unbanUser(userId);
        userMapper.unbanUserInfo(userId);
        postsMapper.unbanPostByUser(userId);
        postCommentsMapper.unbanPcByUser(userId);
        commentCommentsMapper.unbanCcByUser(userId);
        videoMapper.unbanVideoByUser(userId);
        documentMapper.unbanDocByUser(userId);
    }

    /**
     * @desc    ????????????????????????
     * @param userId    ??????id
     * @param targetId  ?????????id
     * @return
     */
    public List<UserReport> getReportBy(Long userId,Long targetId) {
        QueryWrapper<UserReport> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.eq("target_id",targetId);
        return userReportMapper.selectList(wrapper);
    }

    /**
     * @desc    ????????????
     * @param userId    ??????id
     * @param targetId  ?????????id
     * @param sort      ????????????
     * @param reason    ????????????
     */
    public void addReport(Long userId,Long targetId,Short sort,String reason) {
        UserReport userReport = new UserReport();
        userReport.setUser_id(userId);
        userReport.setTarget_id(targetId);
        userReport.setSort(sort);
        userReport.setReason(reason);
        userReportMapper.insert(userReport);
        QueryWrapper<UserReport> wrapper = new QueryWrapper<>();
        wrapper.eq("target_id",targetId);
        Long reportedNum = userReportMapper.selectCount(wrapper);
    }

    /**
     * @desc    ??????????????????
     * @param targetId  ?????????id
     * @param sort      ????????????
     */
    public void delReport(Long targetId,Short sort) {
        // ??????????????????
        QueryWrapper<UserReport> wrapper = new QueryWrapper<>();
        wrapper.eq("target_id",targetId);
        wrapper.eq("sort",sort);
        userReportMapper.delete(wrapper);

    }

    /**
     * @desc    ?????????????????????
     * @param pageNum   ???????
     * @param pageSize  ??????????
     * @return
     */
    public List<ReportVO> getReported(Integer pageNum,Integer pageSize) {
        QueryWrapper<UserReport> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("gmt_modified");
        wrapper.last(" limit "+(pageNum-1)*pageSize+" , "+pageSize);
        List<UserReport> userReports = userReportMapper.selectList(wrapper);
        ArrayList<ReportVO> res = new ArrayList<>();
        for (UserReport userReport : userReports) {
            ReportVO reportVO = new ReportVO();
            reportVO.setReportId(userReport.getId().toString());
            reportVO.setReason(userReport.getReason());

            UserInfo userInfo = userInfoMapper.selectById(userReport.getUser_id());
            if (userInfo!=null) {
                reportVO.setReporterId(userInfo.getId().toString());
                reportVO.setReporterHead(userInfo.getHead());
                reportVO.setReporterName(userInfo.getNickname());
            } else {
                reportVO.setReporterId(null);
                reportVO.setReporterHead(null);
                reportVO.setReporterName("?????????????????????");
            }


            Long targetId = userReport.getTarget_id();
            reportVO.setReportedId(targetId.toString());
            Short sort = userReport.getSort();
            if (sort==0) {
                reportVO.setSort("??????");
                UserInfo target = userInfoMapper.selectById(targetId);
                reportVO.setContent(target.getNickname());
                reportVO.setPhoto(target.getHead());
            } else if (sort==1) {
                reportVO.setSort("??????");
                Posts target = postsMapper.selectById(targetId);
                if (target==null) {
                    reportVO.setContent("??????????????????");
                } else {
                    reportVO.setContent(target.getTitle());
                }
            } else if (sort==2) {
                reportVO.setSort("??????");
                PostComments target = postCommentsMapper.selectById(targetId);
                if (target==null) {
                    reportVO.setContent("??????????????????");
                } else {
                    reportVO.setContent(target.getContent());
                }
            } else if (sort==3) {
                reportVO.setSort("??????");
                CommentComments target = commentCommentsMapper.selectById(targetId);
                if (target==null) {
                    reportVO.setContent("??????????????????");
                } else {
                    reportVO.setContent(target.getContent());
                }
            } else if (sort==4) {
                reportVO.setSort("??????");
                Document document = documentMapper.selectById(targetId);
                if (document==null) {
                    reportVO.setContent("??????????????????");
                } else {
                    reportVO.setContent(document.getName());
                }
            } else if (sort==5) {
                reportVO.setSort("??????");
                Video video = videoMapper.selectById(targetId);
                if (video==null) {
                    reportVO.setContent("??????????????????");
                } else {
                    reportVO.setContent(video.getName());
                }
            }

            res.add(reportVO);
        }
        return res;
    }

    /**
     * @desc    ?????????????????????
     * @return
     */
    public Long getReportedNum() {
        return userReportMapper.selectCount(null);
    }

    /**
     * @desc    ??????????????????????????????
     * @param email ??????
     * @return
     */
    public User getUserByEmail(String email) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email",email);
        return userMapper.selectOne(wrapper);
    }

    /**
     * @desc    ????????????
     * @param userId    ??????id
     * @param answerId  ??????id
     */
    public void like (Long userId,Long answerId) {
        UserLike userLike = new UserLike();
        userLike.setUser_id(userId);
        userLike.setAnswer_id(answerId);
        userLikeMapper.insert(userLike);
        PostComments comment = postCommentsMapper.selectById(answerId);
        comment.setLikes(comment.getLikes()+1);
        postCommentsMapper.updateById(comment);
    }

    /**
     * @desc    ???????????????
     * @param userId    ??????id
     * @param answerId  ??????id
     * @return
     */
    public Boolean isLike(Long userId,Long answerId){
        QueryWrapper<UserLike> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.eq("answer_id",answerId);
        List<UserLike> userLikes = userLikeMapper.selectList(wrapper);
        if (userLikes==null || userLikes.isEmpty()) {
            return false;
        } else {
            return true;
        }

    }

    /**
     * @desc    ??????
     * @param userId    ?????????
     * @param targetId  ?????????id
     * @param sort      1??????|2??????|3??????|4??????
     */
    public void collect (Long userId,Long targetId,Short sort) {
        QueryWrapper<UserCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.eq("target_id",targetId);
        wrapper.eq("sort",sort);
        UserCollection res = userCollectionMapper.selectOne(wrapper);
        if (res==null) {
            UserCollection userCollection = new UserCollection();
            userCollection.setUser_id(userId);
            userCollection.setTarget_id(targetId);
            userCollection.setSort(sort);
            userCollectionMapper.insert(userCollection);
        } else {
            res.setGmt_modified(null);
            userCollectionMapper.updateById(res);
        }
    }

    /**
     * @desc    ????????????
     * @param userId    ??????id
     * @param targetId  ?????????id
     * @param sort      1??????|2??????|3??????
     */
    public void unCollect (Long userId,Long targetId,Short sort) {
        QueryWrapper<UserCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.eq("target_id",targetId);
        wrapper.eq("sort",sort);

        userCollectionMapper.delete(wrapper);
    }

    /**
     * @desc    ??????
     * @param userId    ?????????
     * @param targetId  ?????????id
     * @param sort      1??????|2??????|3??????
     */
    public Boolean isCollected (Long userId,Long targetId,Short sort) {
        QueryWrapper<UserCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.eq("target_id",targetId);
        wrapper.eq("sort",sort);
        Long num = userCollectionMapper.selectCount(wrapper);
        return num != 0;
    }

    /**
     * @desc    ??????????????????
     * @param head  ??????
     * @param userId    ??????id
     * @return
     */
    public String uploadUserHead (MultipartFile head,Long userId) {
        return OssUtils.saveImg(head, ossClient, userId + "", ossPath.getUserHeadPath());
    }

    /**
     * @desc    ????????????id??????????????????
     * @param id    ??????id
     * @return
     */
    public UserVO getById(Long id) {
        User user = userMapper.selectById(id);
        UserInfo userInfo = userInfoMapper.selectById(id);
        UserVO userVO = new UserVO();
        userVO.setUserId(id.toString());
        userVO.setUsername(userInfo.getNickname());
        userVO.setUserhead(userInfo.getHead());
        userVO.setSignature(userInfo.getSignature());
        Boolean gender = userInfo.getGender();
        if (gender == null) {
            userVO.setGender("??????");
        } else if (gender) {
            userVO.setGender("???");
        } else {
            userVO.setGender("???");
        }
        userVO.setSchool(userInfo.getSchool());
        userVO.setAcademic(userInfo.getAcademic());
        userVO.setProfession(userInfo.getProfession());
        userVO.setGrade(userInfo.getGrade().toString());
        userVO.setEmail(user.getEmail());

        return userVO;
    }

    /**
     * @desc    ????????????????????????
     * @param userId    ??????id
     * @param sort      ???????????? 1??????|2??????
     * @param pageNum   ???????
     * @param pageSize  ??????????
     * @return
     */
    public List<MyPost> getMyPostById(Long userId,Short sort,Integer pageNum,Integer pageSize) {
        if (sort==1) {
            ArrayList<MyPost> res = new ArrayList<>();
            QueryWrapper<Posts> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id",userId);
            wrapper.orderByDesc("gmt_modified");
            wrapper.last(" limit "+(pageNum-1)*pageSize+" , "+pageSize);
            List<Posts> posts = postsMapper.selectList(wrapper);
            for (Posts post : posts) {
                MyPost p = new MyPost();
                p.setId(post.getId().toString());
                p.setTitle(post.getTitle());
                p.setDiscussNum(post.getDiscuss_num().toString());
                p.setKind("??????");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String format = sdf.format(post.getGmt_modified());
                p.setDate(format);

                res.add(p);
            }
            return res;
        } else if (sort==2) {
            return userMapper.getMyPost(userId, (pageNum - 1) * pageSize, pageSize);
        } else {
            return null;
        }
    }

    /**
     * @desc    ??????????????????????????????
     * @param userId    ??????id
     * @param sort      1??????|2??????
     * @return
     */
    public Long getMyPostCount(Long userId,Short sort) {
        Long cnt = null;
        if (sort==1) {
            QueryWrapper<Posts> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id",userId);
            cnt = postsMapper.selectCount(wrapper);
        } else if (sort==2) {
            QueryWrapper<Document> docWrapper = new QueryWrapper<>();
            docWrapper.eq("publisher_id",userId);
            cnt = documentMapper.selectCount(docWrapper);
            System.out.println(cnt);
            QueryWrapper<Video> videoWrapper = new QueryWrapper<>();
            videoWrapper.eq("user_id",userId);
            cnt += videoMapper.selectCount(videoWrapper);
        }
        return cnt;
    }

    /**
     * @desc    ????????????????????????
     * @param userId    ??????id
     * @param pageNum   ???????
     * @param pageSize  ??????????
     * @return
     */
    public List<MyJoin> getMyJoinById(Long userId,Integer pageNum,Integer pageSize) {
        QueryWrapper<PostComments> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.orderByDesc("gmt_modified");
        wrapper.last(" limit "+(pageNum-1)*pageSize+" , "+pageSize);
        List<PostComments> postComments = postCommentsMapper.selectList(wrapper);
        ArrayList<MyJoin> res = new ArrayList<>();
        for (PostComments pc : postComments) {
            if (postsMapper.selectById(pc.getPost_id())==null) {
                continue;
            }
            MyJoin myJoin = new MyJoin();
            myJoin.setPostId(pc.getPost_id().toString());
            myJoin.setCommentId(pc.getId().toString());
            myJoin.setTitle(postsMapper.selectById(pc.getPost_id()).getTitle());
            myJoin.setCommentContent(pc.getContent());
            myJoin.setLikes(pc.getLikes().toString());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String format = sdf.format(pc.getGmt_modified());
            myJoin.setDate(format);
            myJoin.setKind("??????");

            res.add(myJoin);
        }

        return res;
    }

    /**
     * @desc    ??????????????????????????????
     * @param userId    ??????id
     * @return
     */
    public Long myJoinNum(Long userId) {
        QueryWrapper<PostComments> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.orderByDesc("gmt_modified");
        return postCommentsMapper.selectCount(wrapper);
    }

    /**
     * @desc    ??????????????????
     * @param userId    ??????id
     * @param targetId  ??????id
     * @param sort  ????????????    1??????|2??????|3??????
     */
    public void addHistory(Long userId,Long targetId,Short sort) {
        // ???????????????????????????????????????
        QueryWrapper<History> historyWrapper = new QueryWrapper<>();
        historyWrapper.eq("user_id",userId);
        historyWrapper.eq("target_id",targetId);
        History his = historyMapper.selectOne(historyWrapper);
        if (his==null) {
            History history = new History();
            history.setUser_id(userId);
            history.setTarget_id(targetId);
            history.setSort(sort);
            historyMapper.insert(history);
        } else {
            his.setGmt_modified(null);
            historyMapper.updateById(his);
        }
    }

    /**
     * @desc    ?????????????????????????????????
     * @param userId    ??????id
     * @param sort      ????????????
     */
    public void delAllHistory(Long userId,Short sort) {
        QueryWrapper<History> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.eq("sort",sort);

        historyMapper.delete(wrapper);
    }

    /**
     * @desc    ????????????????????????
     * @param historyId ????????????id
     */
    public void delHistory(Long historyId) {
        historyMapper.deleteById(historyId);
    }

    /**
     * @desc    ????????????????????????
     * @param sort  ???????????? 1??????|2??????
     * @param userId    ??????id
     * @param pageNum   ???????
     * @param pageSize  ??????????
     * @return
     */
    public List<HistoryVO> getHistory(Short sort,Long userId,Integer pageNum,Integer pageSize) {
        QueryWrapper<History> historyWrapper = new QueryWrapper<>();
        historyWrapper.eq("user_id",userId);
        if (sort==1) {
            historyWrapper.eq("sort",sort);
        } else if (sort==2) {
            // ??????????????????????????????
            historyWrapper.and(wrapper->wrapper.eq("sort",2).or().eq("sort",3));
        }
        historyWrapper.last(" limit "+(pageNum-1)*pageSize+" , "+pageSize);

        List<History> histories = historyMapper.selectList(historyWrapper);
        ArrayList<HistoryVO> res = new ArrayList<>();
        for (History history : histories) {
            HistoryVO historyVO = new HistoryVO();
            historyVO.setHistoryId(history.getId().toString());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            historyVO.setDate(sdf.format(history.getGmt_modified()));
            historyVO.setTargetId(history.getTarget_id().toString());
            if (history.getSort()==1) {
                historyVO.setSort("??????");
                Posts post = postsMapper.selectById(history.getTarget_id());
                if (post!=null) {
                    historyVO.setTitle(post.getTitle());
                    historyVO.setDiscussNum(post.getDiscuss_num().toString());
                } else {
                    historyVO.setTitle("?????????????????????????????????");
                }


            } else if (history.getSort()==2) {
                historyVO.setSort("??????");
                historyVO.setTitle(documentMapper.selectById(history.getTarget_id()).getName());
            } else if (history.getSort()==3) {
                historyVO.setSort("??????");
                historyVO.setTitle(videoMapper.selectById(history.getTarget_id()).getName());
            }
            res.add(historyVO);
        }

        return res;
    }

    /**
     * @desc    ????????????????????????
     * @param sort  ???????????? 1??????|2??????
     * @param userId    ??????id
     * @return
     */
    public Long getHistoryCount(Short sort,Long userId) {
        QueryWrapper<History> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.eq("sort",sort);
        return historyMapper.selectCount(wrapper);
    }

    /**
     * @desc    ????????????????????????
     * @param userId    ??????id
     * @param sort      ???????????? 1??????|2??????|3??????
     * @param pageNum   ???????
     * @param pageSize  ??????????
     * @return
     */
    public List<CollectionVO> getMyCollection(Long userId, Short sort, Integer pageNum, Integer pageSize) {
        QueryWrapper<UserCollection> CollectionWrapper = new QueryWrapper<>();
        CollectionWrapper.eq("user_id",userId);
        if (sort==3) {
            CollectionWrapper.and(wrapper->wrapper.eq("sort",3).or().eq("sort",4));
        }
        CollectionWrapper.eq("sort",sort);
        CollectionWrapper.last(" limit "+(pageNum-1)*pageSize+" , "+pageSize);
        List<UserCollection> userCollections = userCollectionMapper.selectList(CollectionWrapper);
        ArrayList<CollectionVO> res = new ArrayList<>();
        for (UserCollection uc : userCollections) {
            CollectionVO collectionVO = new CollectionVO();
            collectionVO.setId(uc.getId().toString());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            collectionVO.setDate(sdf.format(uc.getGmt_modified()));
            collectionVO.setDiscussNum(userService.getCollectNum(uc.getTarget_id(),sort).toString());
            collectionVO.setTargetId(uc.getTarget_id().toString());
            if (sort==1) {  // ??????
                collectionVO.setTitle(postsMapper.selectById(uc.getTarget_id()).getTitle());
                collectionVO.setSort("??????");
            } else if (sort==2) {   // ??????
                collectionVO.setSort("??????");
                PostComments pc = postCommentsMapper.selectById(uc.getTarget_id());
                Posts post = postsMapper.selectById(pc.getPost_id());
                if (post==null) {
                    continue;
                } else {
                    collectionVO.setTitle(post.getTitle());
                }
                collectionVO.setContent(pc.getContent());
            } else if (sort==3) {   // ??????
                if (uc.getSort()==3) {
                    collectionVO.setSort("??????");
                    collectionVO.setTitle(documentMapper.selectById(uc.getTarget_id()).getName());
                } else if (uc.getSort()==4) {
                    collectionVO.setSort("??????");
                    collectionVO.setTitle(videoMapper.selectById(uc.getTarget_id()).getName());
                }
            }
            res.add(collectionVO);
        }
        return res;
    }

    /**
     * @desc    ?????????????????????
     * @param userId    ??????id
     * @param sort  ???????????? 1??????|2??????|3??????
     * @return
     */
    public Long getUserCollectionNum(Long userId, Short sort) {
        QueryWrapper<UserCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.eq("sort",sort);
        return userCollectionMapper.selectCount(wrapper);
    }

    /**
     * @desc    ??????????????????
     * @param pageNum   ???????
     * @param pageSize  ??????????
     * @return
     */
    public List<BanUserVO> getBanUserList(Integer pageNum,Integer pageSize) {
        LocalDate localDate = LocalDate.now();
        LocalDate startTime = localDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endTime = localDate.with(TemporalAdjusters.lastDayOfMonth());
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String start = df.format(startTime)+" 00:00:00";
        String end = df.format(endTime)+" 23:59:59";

        List<BanUserVO> violationList = userMapper.getViolationList(start, end, (pageNum-1)*pageSize, pageSize);
        for (BanUserVO banUserVO : violationList) {
            UserInfo userInfo = userInfoMapper.getById(Long.valueOf(banUserVO.getUserId()));
            if (!userInfo.is_deleted()) {
                banUserVO.setUserName(userInfo.getNickname());
                banUserVO.setUserHead(userInfo.getHead());
                banUserVO.setStatus("??????");
            } else {

                banUserVO.setUserName(userInfo.getNickname());
                banUserVO.setUserHead(userInfo.getHead());
                banUserVO.setStatus("?????????");
            }
        }
        return violationList;
    }

    /**
     * @desc    ????????????????????????
     * @param targetId  id
     * @param sort  ???????????? 1??????|2??????|3??????
     * @return
     */
    public Long getCollectNum(Long targetId,Short sort) {
        QueryWrapper<UserCollection> collectionWrapper = new QueryWrapper<>();
        collectionWrapper.eq("sort",sort);
        collectionWrapper.eq("target_id",targetId);
        return userCollectionMapper.selectCount(collectionWrapper);
    }


}
