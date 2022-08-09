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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

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

    public void register(UserRegDTO userRegDTO) {
        User user = new User();
        UserInfo userInfo = new UserInfo();

        long id = IdWorker.getId(user);
        user.setId(id);
        user.setEmail(userRegDTO.getEmail());
        user.setPassword(encoder.encode(userRegDTO.getPassword()));
        user.setRole("ROLE_USER");
        userInfo.setId(id);
//        userInfo.setNickname(id+"用户");

        userMapper.insert(user);
        userInfoMapper.insert(userInfo);
    }

    public void updPwd(@RequestBody UserDTO userDTO) {
        String password = userDTO.getPassword();
        password = encoder.encode(password);
        User newPwdUser = getUserByEmail(userDTO.getUsername());
        newPwdUser.setPassword(password);
        userMapper.updateById(newPwdUser);
    }

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

    public User getUserByEmail(String email) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email",email);
        return userMapper.selectOne(wrapper);
    }


    public void like (Long userId,Long answerId) {
        UserLike userLike = new UserLike();
        userLike.setUser_id(userId);
        userLike.setAnswer_id(answerId);
        userLikeMapper.insert(userLike);
        PostComments comment = postCommentsMapper.selectById(answerId);
        comment.setLikes(comment.getLikes()+1);
        postCommentsMapper.updateById(comment);
    }

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
     * @desc    收藏
     * @param userId    用户名
     * @param targetId  收藏品id
     * @param sort      1帖子|2回答|3文档
     */
    public void collect (Long userId,Long targetId,Short sort) {
        UserCollection userCollection = new UserCollection();
        userCollection.setUser_id(userId);
        userCollection.setTarget_id(targetId);
        userCollection.setSort(sort);
        userCollectionMapper.insert(userCollection);
    }

    public void unCollect (Long userId,Long targetId,Short sort) {
        QueryWrapper<UserCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.eq("target_id",targetId);
        wrapper.eq("sort",sort);

        userCollectionMapper.delete(wrapper);
    }

    /**
     * @desc    收藏
     * @param userId    用户名
     * @param targetId  收藏品id
     * @param sort      1帖子|2回答|3文档
     */
    public Boolean isCollected (Long userId,Long targetId,Short sort) {
        QueryWrapper<UserCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.eq("target_id",targetId);
        wrapper.eq("sort",sort);
        UserCollection result = userCollectionMapper.selectOne(wrapper);
        if (result==null) {
            return false;
        } else {
            return true;
        }
    }

    public String uploadUserHead (MultipartFile head,Long userId) {
        return OssUtils.saveImg(head, ossClient, userId + "", ossPath.getUserHeadPath());
    }

    public UserVO getById(Long id) {
        User user = userMapper.selectById(id);
        UserInfo userInfo = userInfoMapper.selectById(id);
        UserVO userVO = new UserVO();
        userVO.setUserId(id.toString());
        userVO.setUsername(userInfo.getNickname());
        userVO.setSignature(userInfo.getSignature());
        Boolean gender = userInfo.getGender();
        if (gender == null) {
            userVO.setGender("不详");
        } else if (gender) {
            userVO.setGender("男");
        } else {
            userVO.setGender("女");
        }
        userVO.setSchool(userInfo.getSchool());
        userVO.setAcademic(userInfo.getAcademic());
        userVO.setProfession(userInfo.getProfession());
        userVO.setGrade(userInfo.getGrade().toString());
        userVO.setEmail(user.getEmail());

        return userVO;
    }

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
                p.setKind("帖子");
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

    public Long getMyPostCount(Long userId) {
        QueryWrapper<Posts> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        return postsMapper.selectCount(wrapper);
    }

    public List<MyJoin> getMyJoinById(Long userId,Integer pageNum,Integer pageSize) {
        QueryWrapper<PostComments> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.orderByDesc("gmt_modified");
        wrapper.last(" limit "+(pageNum-1)*pageSize+" , "+pageSize);
        List<PostComments> postComments = postCommentsMapper.selectList(wrapper);
        ArrayList<MyJoin> res = new ArrayList<>();
        for (PostComments pc : postComments) {
            MyJoin myJoin = new MyJoin();
            myJoin.setPostId(pc.getPost_id().toString());
            myJoin.setCommentId(pc.getId().toString());
            myJoin.setTitle(postsMapper.selectById(pc.getPost_id()).getTitle());
            myJoin.setCommentContent(pc.getContent());
            myJoin.setLikes(pc.getLikes().toString());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String format = sdf.format(pc.getGmt_modified());
            myJoin.setDate(format);

            res.add(myJoin);
        }
        return res;
    }

    public void addHistory(Long userId,Long targetId,Short sort) {
        History history = new History();
        history.setUser_id(userId);
        history.setTarget_id(targetId);
        history.setSort(sort);

        historyMapper.insert(history);
    }

    public void delAllHistory(Long userId,Short sort) {
        QueryWrapper<History> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.eq("sort",sort);

        historyMapper.delete(wrapper);
    }

    public void delHistory(Long historyId) {
        historyMapper.deleteById(historyId);
    }


    public List<HistoryVO> getHistory(Short sort,Long userId,Integer pageNum,Integer pageSize) {
        QueryWrapper<History> historyWrapper = new QueryWrapper<>();
        historyWrapper.eq("user_id",userId);
        if (sort==1) {
            historyWrapper.eq("sort",sort);
        } else if (sort==2) {
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
            if (history.getSort()==1) {
                historyVO.setSort("帖子");
                historyVO.setTitle(postsMapper.selectById(history.getTarget_id()).getTitle());
            } else if (history.getSort()==2) {
                historyVO.setSort("文档");
                historyVO.setTitle(documentMapper.selectById(history.getTarget_id()).getName());
            } else if (history.getSort()==3) {
                historyVO.setSort("视频");
                historyVO.setTitle(videoMapper.selectById(history.getTarget_id()).getName());
            }
            res.add(historyVO);
        }

        return res;
    }

    public Long getHistoryCount(Short sort,Long userId) {
        QueryWrapper<History> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.eq("sort",sort);
        return historyMapper.selectCount(wrapper);
    }

    public void stopFocus(Long todoId,Integer time) {
        Todo todo = todoMapper.selectById(todoId);
        todo.setTime(time);
        todoMapper.updateById(todo);
    }

    /**
     * @desc 获取专注
     * @param userId    用户id
     * @param sort      1本周|2本月
     * @return
     */
    public List<TodoVO> getFocus(Long userId,Short sort,Integer pageNum,Integer pageSize) {
        LocalDate localDate = LocalDate.now();
        LocalDate startTime;
        LocalDate endTime;
        if (sort==1) {
            startTime = localDate.with(DayOfWeek.MONDAY);
            endTime = localDate.with(DayOfWeek.SUNDAY);
        } else {
            startTime = localDate.with(TemporalAdjusters.firstDayOfMonth());
            endTime = localDate.with(TemporalAdjusters.lastDayOfMonth());
        }
        QueryWrapper<Todo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.between("gmt_modified",startTime,endTime);
        wrapper.last(" limit "+(pageNum-1)*pageSize+" , "+pageSize);
        List<Todo> todos = todoMapper.selectList(wrapper);
        ArrayList<TodoVO> res = new ArrayList<>();
        Integer rank = (pageNum-1)*pageSize+1;
        for (Todo todo : todos) {
            TodoVO todoVO = new TodoVO();
            todoVO.setRank(rank++);
            todoVO.setId(todo.getId().toString());
            todoVO.setContent(todoVO.getContent());
            todoVO.setTime(todo.getTime().toString());
            res.add(todoVO);
        }
        return res;
    }

    public Long getFocusCount(Long userId,Short sort) {
        LocalDate localDate = LocalDate.now();
        LocalDate startTime;
        LocalDate endTime;
        if (sort==1) {
            startTime = localDate.with(DayOfWeek.MONDAY);
            endTime = localDate.with(DayOfWeek.SUNDAY);
        } else {
            startTime = localDate.with(TemporalAdjusters.firstDayOfMonth());
            endTime = localDate.with(TemporalAdjusters.lastDayOfMonth());
        }
        QueryWrapper<Todo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.between("gmt_modified",startTime,endTime);
        return todoMapper.selectCount(wrapper);
    }

    public List<CollectionVO> getMyCollection(Long userId, Short sort, Integer pageNum, Integer pageSize) {
        QueryWrapper<UserCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.eq("sort",sort);
        wrapper.last(" limit "+(pageNum-1)*pageSize+" , "+pageSize);
        List<UserCollection> userCollections = userCollectionMapper.selectList(wrapper);
        ArrayList<CollectionVO> res = new ArrayList<>();
        for (UserCollection uc : userCollections) {
            CollectionVO collectionVO = new CollectionVO();
            collectionVO.setId(uc.getId().toString());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            collectionVO.setDate(sdf.format(uc.getGmt_modified()));
            if (sort==0) {  // 帖子
                collectionVO.setTitle(postsMapper.selectById(uc.getTarget_id()).getTitle());
                collectionVO.setSort("帖子");
            } else if (sort==1) {   // 回复
                collectionVO.setTitle(postCommentsMapper.selectById(uc.getTarget_id()).getContent());
            } else if (sort==2) {   // 文档
                collectionVO.setTitle(documentMapper.selectById(uc.getTarget_id()).getName());
            }
            res.add(collectionVO);
        }
        return res;
    }


}
