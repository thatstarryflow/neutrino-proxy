/**
 * Copyright (c) 2022 aoshiguchen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package fun.asgc.neutrino.proxy.server.service;

import fun.asgc.neutrino.core.annotation.Autowired;
import fun.asgc.neutrino.core.annotation.Component;
import fun.asgc.neutrino.core.db.page.Page;
import fun.asgc.neutrino.core.db.page.PageQuery;
import fun.asgc.neutrino.core.util.CollectionUtil;
import fun.asgc.neutrino.proxy.server.base.rest.constant.EnableStatusEnum;
import fun.asgc.neutrino.proxy.server.base.rest.constant.OnlineStatusEnum;
import fun.asgc.neutrino.proxy.server.controller.req.LicenseCreateReq;
import fun.asgc.neutrino.proxy.server.controller.req.LicenseListReq;
import fun.asgc.neutrino.proxy.server.controller.req.LicenseUpdateEnableStatusReq;
import fun.asgc.neutrino.proxy.server.controller.req.LicenseUpdateReq;
import fun.asgc.neutrino.proxy.server.controller.res.*;
import fun.asgc.neutrino.proxy.server.dal.LicenseMapper;
import fun.asgc.neutrino.proxy.server.dal.UserMapper;
import fun.asgc.neutrino.proxy.server.dal.entity.LicenseDO;
import fun.asgc.neutrino.proxy.server.dal.entity.UserDO;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * license服务
 * @author: aoshiguchen
 * @date: 2022/8/6
 */
@Component
public class LicenseService {

	@Autowired
	private LicenseMapper licenseMapper;
	@Autowired
	private UserMapper userMapper;

	public Page<LicenseListRes> page(PageQuery pageQuery, LicenseListReq req) {
		Page<LicenseListRes> page = Page.create(pageQuery);
		licenseMapper.page(page, req);
		if (!CollectionUtil.isEmpty(page.getRecords())) {
			Set<Integer> userIds = page.getRecords().stream().map(LicenseListRes::getUserId).collect(Collectors.toSet());
			List<UserDO> userList = userMapper.findByIds(userIds);
			Map<Integer, UserDO> userMap = userList.stream().collect(Collectors.toMap(UserDO::getId, Function.identity()));
			for (LicenseListRes item : page.getRecords()) {
				UserDO userDO = userMap.get(item.getUserId());
				if (null != userDO) {
					item.setUserName(userDO.getName());
				}
			}
		}
		return page;
	}

	/**
	 * 创建license
	 * @param req
	 * @return
	 */
	public LicenseCreateRes create(LicenseCreateReq req) {
		String key = UUID.randomUUID().toString().replaceAll("-", "");
		Date now = new Date();

		licenseMapper.add(new LicenseDO()
			.setName(req.getName())
			.setKey(key)
			.setUserId(req.getUserId())
			.setIsOnline(OnlineStatusEnum.OFFLINE.getStatus())
			.setEnable(EnableStatusEnum.ENABLE.getStatus())
			.setCreateTime(now)
			.setUpdateTime(now)
		);
		return new LicenseCreateRes();
	}

	public LicenseUpdateRes update(LicenseUpdateReq req) {
		licenseMapper.update(req.getId(), req.getName());
		return new LicenseUpdateRes();
	}

	public LicenseDetailRes detail(Integer id) {
		LicenseDO licenseDO = licenseMapper.findById(id);
		if (null == licenseDO) {
			return null;
		}
		UserDO userDO = userMapper.findById(licenseDO.getUserId());
		String userName = "";
		if (null != userDO) {
			userName = userDO.getName();
		}
		return new LicenseDetailRes()
			.setId(licenseDO.getId())
			.setName(licenseDO.getName())
			.setKey(licenseDO.getKey())
			.setUserId(licenseDO.getUserId())
			.setUserName(userName)
			.setIsOnline(licenseDO.getIsOnline())
			.setEnable(licenseDO.getEnable())
			.setCreateTime(licenseDO.getCreateTime())
			.setUpdateTime(licenseDO.getUpdateTime())
		;
	}

	/**
	 * 更新license启用状态
	 * @param req
	 * @return
	 */
	public LicenseUpdateEnableStatusRes updateEnableStatus(LicenseUpdateEnableStatusReq req) {
		licenseMapper.updateEnableStatus(req.getId(), req.getEnable());

		return new LicenseUpdateEnableStatusRes();
	}

	/**
	 * 删除license
	 * @param id
	 */
	public void delete(Integer id) {
		licenseMapper.delete(id);
	}

	/**
	 * 重置license
	 * @param id
	 */
	public void reset(Integer id) {
		String key = UUID.randomUUID().toString().replaceAll("-", "");
		Date now = new Date();

		licenseMapper.reset(id, key, now);
	}

}
