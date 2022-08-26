package com.lyd.controller;

import cn.hutool.core.util.StrUtil;
import com.google.code.kaptcha.Producer;
import com.lyd.common.Constants;
import com.lyd.common.Result;
import com.lyd.controller.VO.*;
import com.lyd.controller.dto.UserInfoDTO;
import com.lyd.controller.dto.UserDTO;
import com.lyd.controller.dto.UserRegDTO;
import com.lyd.entity.User;
import com.lyd.mapper.*;
import com.lyd.service.DocumentService;
import com.lyd.service.UserService;
import com.lyd.utils.RedisUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.List;

@Slf4j
@ResponseBody
@RestController
@Controller
@RequestMapping("/user")
@CrossOrigin(origins ="*")
@Api("用户接口")
public class UserController {

    @Resource
    private Producer producer;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private RedisUtils redisUtils;
    @Resource
    private UserMapper userMapper;
    @Resource
    private HistoryMapper historyMapper;
    @Resource
    private TodoMapper todoMapper;
    @Resource
    private PostsMapper postsMapper;
    @Resource
    private PostCommentsMapper postCommentsMapper;
    @Resource
    private DocumentMapper documentMapper;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private BCryptPasswordEncoder encoder;
    @Resource
    private VideoMapper videoMapper;

    @ApiOperation("注册")
    @PostMapping("/register")
    public Result register(@RequestBody UserRegDTO userRegDTO) {
        log.info("访问了/user/register接口");
        User userByEmail = userService.getUserByEmail(userRegDTO.getEmail());
        if (userByEmail!=null) {
            return Result.error(Constants.CODE_400,"该邮箱已被注册");
        } else {
            userService.register(userRegDTO);
            return Result.success();
        }
    }

    @ApiOperation("登录")
    @PostMapping("/login")
    public Result login(@RequestBody UserDTO userDTO) {
        log.info("访问了/user/login接口");
        String email = userDTO.getUsername();
        String password = userDTO.getPassword();
        User us = userService.getUserByEmail(email);
        if (us==null) {
            return Result.error(Constants.CODE_400,"该邮箱未被注册");
        }
        if (StrUtil.isBlank(email) || StrUtil.isBlank(password)) {
            return Result.error(Constants.CODE_400, "参数错误");
        }
        if (!encoder.matches(password,us.getPassword())) {
            return  Result.error(Constants.CODE_401,"密码错误");
        }
        UserLoginVO userLoginVO = new UserLoginVO();
        userLoginVO.setUserId(us.getId().toString());
        userLoginVO.setEmail(email);
        userLoginVO.setPassword(encoder.encode(password));
        userLoginVO.setAuthority(us.getRole());
        return Result.success(userLoginVO);
    }

    @ApiOperation("修改密码")
    @PostMapping("/updPwd")
    public Result updatePwd(@RequestBody UserDTO userDTO) {
        log.info("访问了/user/login接口");
        String email = userDTO.getUsername();
        String password = userDTO.getPassword();
        User us = userService.getUserByEmail(email);
        if (us==null) {
            return Result.error(Constants.CODE_400,"该邮箱未被注册");
        }
        if (StrUtil.isBlank(email) || StrUtil.isBlank(password)) {
            return Result.error(Constants.CODE_400, "参数错误");
        }
        userService.updPwd(userDTO);
        return Result.success();
    }


    @ApiOperation("修改/完善用户信息")
    @PostMapping("/upd")
    public Result updUser(@RequestBody UserInfoDTO userInfoDTO) {
        log.info(userInfoDTO.toString());
        Long userId = Long.valueOf(userInfoDTO.getUserId());
        log.info("访问了/user/upd接口");
        if (userMapper.selectById(userId)==null) {
            return Result.error(Constants.CODE_400,"不存在该用户");
        }
        userService.updUserInfo(userInfoDTO);
        return Result.success();
    }





    /**
     * 获取验证码
     */
    @ApiOperation("获取图片验证码")
    @GetMapping("/getCode/{key}")
    public void getKaptcha(@PathVariable String key,HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("访问了/user/getCode/{}接口",key);
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");
        //这里的text即为kaptcha生成的验证码中的文字，生成后放入session中，客户端请求登录时再取出进行比对
//        log.info("sessionId : "+request.getSession().getId());
        String text = producer.createText();
        log.info("生成验证码:{}", text);
        //获取运算式文本
        String mathStr = text.substring(text.lastIndexOf('^')+1, text.lastIndexOf('?')+1);
        //获取运算式结果
        String result = text.substring(text.lastIndexOf("@") + 1);
        log.info("验证码运算式:{}",mathStr);
        log.info("验证码计算结果:{}",result);
//        HttpSession session = request.getSession();
//        session.removeAttribute("code");
//        session.setAttribute("code", result);
//        redisTemplate.opsForValue().set(key, result);
        redisUtils.set(key,result,10*60*1000);

        BufferedImage image = producer.createImage(text);
        OutputStream os =  response.getOutputStream();
        ImageIO.write(image, "jpg", os);
        IOUtils.closeQuietly(os);
    }

    /**
     * @desc 验证验证码是否正确
     */
    @ApiOperation("验证图片验证码")
    @GetMapping("/checkCode/{code}/{key}")
    public Result checkKaptcha(@PathVariable String key,@PathVariable String code,HttpServletRequest request){
        log.info("访问了/user/checkCode/{}/{}接口",code,key);
//        log.info("sessionId : "+request.getSession().getId());
//        String text = request.getSession().getAttribute("code").toString();
        String text = (String) redisUtils.get(key);
        log.info("验证:正确答案{}",text);
        log.info("验证:用户输入{}",code);
        if (code.equals(text)) {
            return Result.success("验证码校验成功");
        } else {
            return Result.error(Constants.CODE_400,"验证码校验失败");
        }
    }


    @ApiOperation("上传用户头像")
    @PostMapping("/upHead/{userId}")
    public Result uploadHead(@RequestParam MultipartFile multipartFile,@PathVariable Long userId) {
        log.info("访问了/user/upHead接口");
        if (userMapper.selectById(userId)==null) {
            return Result.error(Constants.CODE_400,"不存在该用户");
        }
        String path = userService.uploadUserHead(multipartFile, userId);

        return Result.success(path);
    }

    @ApiOperation("获取用户详细信息")
    @GetMapping("/{userId}")
    public Result getById(@PathVariable Long userId) {
        log.info("访问了/user/{}",userId);
        if (userMapper.selectById(userId)==null) {
            return Result.error(Constants.CODE_400,"不存在该用户");
        }
        UserVO byId = userService.getById(userId);

        return Result.success(byId);
    }

    @ApiOperation("我发布的(sort:1帖子|2资料)")
    @GetMapping("/myPost/{userId}/{sort}/{pageNum}/{pageSize}")
    public Result getMyPost(@PathVariable Long userId,@PathVariable Short sort,@PathVariable Integer pageNum,@PathVariable Integer pageSize) {
        log.info("访问了/user/myPost/{}/{}/{}",userId,pageNum,pageSize);
        if (sort!=1 && sort!=2) {
            return Result.error(Constants.CODE_400,"sort:1帖子|2资料");
        }
        if (pageNum<1) {
            return Result.error(Constants.CODE_400,"页码从第1页开始");
        }
        if (userMapper.selectById(userId)==null) {
            return Result.error(Constants.CODE_400,"不存在该用户");
        }

        List<MyPost> myPostById = userService.getMyPostById(userId,sort, pageNum, pageSize);
        return Result.success(myPostById,userService.getMyPostCount(userId,sort));
    }

    @ApiOperation("我参与的")
    @GetMapping("/myJoin/{userId}/{pageNum}/{pageSize}")
    public Result getMyJoin(@PathVariable Long userId,@PathVariable Integer pageNum,@PathVariable Integer pageSize) {
        log.info("访问了/user/myJoin/{}/{}/{}",userId,pageNum,pageSize);
        if (pageNum<1) {
            return Result.error(Constants.CODE_400,"页码从第1页开始");
        }
        if (userMapper.selectById(userId)==null) {
            return Result.error(Constants.CODE_400,"不存在该用户");
        }
        List<MyJoin> myJoinById = userService.getMyJoinById(userId, pageNum, pageSize);

        return Result.success(myJoinById,userService.myJoinNum(userId));
    }

    @ApiOperation("历史记录 (sort:1帖子|2资料)")
    @GetMapping("/myHistory/{sort}/{userId}/{pageNum}/{pageSize}")
    public Result getMyHistory(@PathVariable Short sort,@PathVariable Long userId,@PathVariable Integer pageNum,@PathVariable Integer pageSize) {
        log.info("访问了/user/myHistory/{}/{}/{}/{}",sort,userId,pageNum,pageSize);
        if (sort!=1 && sort!=2) {
            return Result.error(Constants.CODE_400,"sort:1帖子|2资料");
        }
        if (pageNum<1) {
            return Result.error(Constants.CODE_400,"页码从第1页开始");
        }
        if (userMapper.selectById(userId)==null) {
            return Result.error(Constants.CODE_400,"不存在该用户");
        }
        List<HistoryVO> history = userService.getHistory(sort, userId, pageNum, pageSize);
        return Result.success(history,userService.getHistoryCount(sort,userId));
    }

    @ApiOperation("删除某条历史记录")
    @DeleteMapping("/delHistory/{historyId}")
    public Result delHistory(@PathVariable Long historyId) {
        log.info("访问了/user/delHistory/{}",historyId);
        if (historyMapper.selectById(historyId)==null) {
            return Result.error(Constants.CODE_400,"不存在该历史记录");
        }
        userService.delHistory(historyId);
        return Result.success();
    }

    @ApiOperation("删除所有历史记录")
    @DeleteMapping("/delAllHistory/{sort}/{userId}")
    public Result delAllHistory(@PathVariable Short sort,@PathVariable Long userId) {
        log.info("访问了/user/delAllHistory/{}/{}",sort,userId);
        if (sort!=1 && sort!=2) {
            return Result.error(Constants.CODE_400,"sort:1帖子|2资料");
        }
        if (userMapper.selectById(userId)==null) {
            return Result.error(Constants.CODE_400,"不存在该用户");
        }
        userService.delAllHistory(userId,sort);
        return Result.success();
    }


    @ApiOperation("获取收藏(sort:1帖子|2回答|3资料)")
    @GetMapping("/collection/{sort}/{userId}/{pageNum}/{pageSize}")
    public Result getCollection(@PathVariable Short sort,@PathVariable Long userId,@PathVariable Integer pageNum,@PathVariable Integer pageSize) {
        log.info("访问了/user/collection/{}/{}/{}/{}",sort,userId,pageNum,pageSize);
        if (userMapper.selectById(userId)==null) {
            return Result.error(Constants.CODE_400,"不存在该用户");
        }
        if (sort!=1 && sort!=2 && sort!=3) {
            return Result.error(Constants.CODE_400,"sort:1帖子|2回答|3资料");
        }
        if (pageNum<1) {
            return Result.error(Constants.CODE_400,"从第一页开始");
        }
        List<CollectionVO> myCollection = userService.getMyCollection(userId, sort, pageNum, pageSize);
        return Result.success(myCollection, userService.getUserCollectionNum(userId,sort));
    }

    @ApiOperation("给帖子的回答点赞")
    @GetMapping("/like/{userId}/{answerId}")
    public Result like(@PathVariable Long userId,@PathVariable Long answerId) {
        log.info("访问了/#/like/{}/{}",userId,answerId);
        if (userMapper.selectById(userId)==null) {
            return Result.error(Constants.CODE_400,"不存在该用户");
        }
        if (postCommentsMapper.selectById(answerId)==null) {
            return Result.error(Constants.CODE_400,"不存在该回答");
        }
        userService.like(userId,answerId);
        return Result.success();
    }

    @ApiOperation("收藏 (sort:1帖子|2回答|3文档|4视频)")
    @GetMapping("/collect/post/{sort}/{userId}/{targetId}")
    public Result collectPost(@PathVariable Long userId,@PathVariable Long targetId,@PathVariable Short sort) {
        log.info("访问了/#/collect/post/{}/{}/{}",sort,userId,targetId);
        if (userMapper.selectById(userId)==null) {
            return Result.error(Constants.CODE_400,"不存在该用户");
        }
        if (sort!=1 && sort!=2 && sort!=3 && sort!=4) {
            return Result.error(Constants.CODE_400,"sort:1帖子|2回答|3文档|4视频");
        }
        if (sort==1 && postsMapper.selectById(targetId)==null) {
            return Result.error(Constants.CODE_400,"不存在该帖子");
        }
        if (sort==2 && postCommentsMapper.selectById(targetId)==null) {
            return Result.error(Constants.CODE_400,"不存在该回答");
        }
        if (sort==3 && documentMapper.selectById(targetId)==null) {
            return Result.error(Constants.CODE_400,"不存在该文档");
        }
        if (sort==3 && videoMapper.selectById(targetId)==null) {
            return Result.error(Constants.CODE_400,"不存在该视频");
        }
        userService.collect(userId,targetId,sort);
        return Result.success();
    }

    @ApiOperation("取消收藏 (sort:1帖子|2回答|3文档|4视频)")
    @GetMapping("/unCollect/post/{sort}/{userId}/{targetId}")
    public Result unCollectPost(@PathVariable Long userId,@PathVariable Long targetId,@PathVariable Short sort) {
        log.info("访问了/#/unCollect/post/{}/{}/{}",sort,userId,targetId);
        if (userMapper.selectById(userId)==null) {
            return Result.error(Constants.CODE_400,"不存在该用户");
        }
        if (sort!=1 && sort!=2 && sort!=3 && sort!=4) {
            return Result.error(Constants.CODE_400,"sort:1帖子|2回答|3文档|4视频");
        }
        if (sort==1 && postsMapper.selectById(targetId)==null) {
            return Result.error(Constants.CODE_400,"不存在该帖子");
        }
        if (sort==2 && postCommentsMapper.selectById(targetId)==null) {
            return Result.error(Constants.CODE_400,"不存在该回答");
        }
        if (sort==3 && documentMapper.selectById(targetId)==null) {
            return Result.error(Constants.CODE_400,"不存在该文档");
        }
        if (sort==4 && videoMapper.selectById(targetId)==null) {
            return Result.error(Constants.CODE_400,"不存在该视频");
        }
        userService.unCollect(userId,targetId,sort);
        return Result.success();
    }






}
