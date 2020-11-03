package es.fernandopal.yato.core.rank;

import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.fernandopal.yato.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public class RankManager {
//	private static final Logger LOGGER = LoggerFactory.getLogger(RankManager.class);
//
//	private final Guild yServer = Main.getGuildById(703263711971901450L);
//
//	private final Role supporter = yServer.getRoleById("708757214567071754");
//	private final Role yatoLover = yServer.getRoleById("708758319468249189");
//	private final Role chonk = yServer.getRoleById("708758431045124166");
//
//	public Rank getRank(User u) {
//		if(u.getId().equals(Config.get("OWNER_ID"))) {
//			return Rank.CHONK;
//		} else {
//			if(!isOnServer(u)) {
//				return Rank.FREE_TIER;
//			} else {
//				final Member member = yServer.getMember(u);
//
//				if(member == null) { return Rank.FREE_TIER; }
//
//				final List<Role> roles = member.getRoles();
//				if(roles.contains(supporter)) {
//					return Rank.SUPPORTER;
//				} else if(roles.contains(yatoLover)) {
//					return Rank.YATO_LOVER;
//				} else if(roles.contains(chonk)) {
//					return Rank.CHONK;
//				} else {
//					return Rank.FREE_TIER;
//				}
//			}
//		}
//	}
//
//	public void setRank(Rank r, User u) {
//		final Member member = yServer.getMember(u);
//
//		if(member != null) {
//			if (isOnServer(u)) {
//				member.getRoles().add(getRoleForRank(r));
//			} else {
//				LOGGER.warn("The user '" + u.toString() + "' is not on the official server, I can't set the rank you requested.");
//			}
//		}
//	}
//
//	public void removeRank(Rank r, User u) {
//		final Member member = yServer.getMember(u);
//
//		if(member != null) {
//			if (isOnServer(u)) {
//				member.getRoles().remove(getRoleForRank(r));
//			} else {
//				LOGGER.warn("The user '" + u.toString() + "' is not on the official server, I can't remove the rank you requested.");
//			}
//		}
//	}
//
//	public Role getRoleForRank(Rank r) {
//		if(r.equals(Rank.SUPPORTER)) {
//			return supporter;
//		} else if(r.equals(Rank.YATO_LOVER)) {
//			return yatoLover;
//		} else if(r.equals(Rank.CHONK)) {
//			return chonk;
//		} else {
//			return null;
//		}
//	}
//
//	public boolean isOnServer(User u) {
//		return yServer.getMember(u) != null;
//	}
}
