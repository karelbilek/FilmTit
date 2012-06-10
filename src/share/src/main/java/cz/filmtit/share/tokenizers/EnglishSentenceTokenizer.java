package cz.filmtit.share.tokenizers;


import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.google.common.base.Strings;
import com.google.gwt.regexp.shared.RegExp;


public class EnglishSentenceTokenizer extends SentenceTokenizer {


        // Check out the private methods for comments and examples about these
    // regular expressions:

    @Override
    public String nonStandardLetters() { 
        return  ""; 
    }


    @Override
    protected String[] getMonthNames() {
        String[] monthNames = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        return monthNames;
    }
   
    @Override
    protected String[] getAbbrevList() {
        String[] abbrevList = {
                
                "Mr", "Mrs", "No", "pp", "St", "no",
            "Sr", "Jr", "Bros", "etc", "vs", "esp", "Fig", "fig", "Jan", "Feb", "Mar", "Apr", "Jun", "Jul",
            "Aug", "Sep", "Sept", "Oct", "Okt", "Nov", "Dec", "Ph.D", "PhD",
            "al",  // in "et al."
            "cf", "Inc", "Ms", "Gen", "Sen", "Prof", "Corp", "Co",
            
            //FROM WIKTIONARY
            
            "a", "A", "a.e", "a.l", "a.m", "a.o.b", "abb", "abbr", "abbrev", "abr", "acc", "Act", "act", "ad", "adj", "adm", "admin", "aff", "Ag", "al", "Ala", "amer", "Amer", "Amo", "amp. hr", "ans", "approx", "appt", "Apr", "apt", "arch", "Ariz", "Ark", "assoc", "Au.D", "aud", "Aug", "Av", "avg", "b", "B", "B.Acc", "B.Acy", "B.Arch", "B.B.A", "B.C.E", "B.Comm", "B.Des", "B.E", "B.E.E", "B.Ed", "B.Eng", "B.F.A", "B.L.S", "B.Lib", "B.Lit", "B.M.E", "bankr", "Bel &amp; Dr", "bet", "Bg", "bio", "biol", "bldg", "blvd", "bn", "Bp", "Br", "brevic", "bros", "bus", "C",
 "C.A.A.F", "C.A.F.C", "C.D", "C.D. Cal", "C.D. Ill", "c.l", "C.Z", "ca", "Cal", "Calif", "Can", "Canad", "Cant", "CC", "Cdn", "cert", "cf", "cfr", "ch", "chem", "Chron", "ChSl", "Cir", "Cl", "co-r.e", "co", "Co", "Col", "col", "coll", "Colo", "Coloss", "Com", "concr", "conf", "Cong", "Conn", "connex", "constr", "Cor", "corp", "cp", "Cr", "Ct", "Ct. of App", "ctr", "Cyr", "D", "D. Ariz", "D. Colo", "D. Conn", "D. Del", "D. Haw", "D. Kan", "D. Mass", "D. Md", "D. Me", "D. Minn", "D. Mont", "D. Neb", "D. Nev", "D. Or", "D. Vt", "D. Wyo", "D.Arch", "D.C. Cir", "D.C.Z", "D.D", "D.D.C", "D.N.D", "D.N.H", "D.N.J", "D.N.M", "D.O", "D.P.R", "D.R.I", "D.S.C", "D.S.D", "D.Sc", "D.U.I", "D.V.I", "D.W.I", "Dan", "Dat", "Dec", "deg", "dept", "Deu", "Deut", "diam", "dim", "do", "dom", "doz", "Dr", "Dr.DES", "Du", "E.D", "E.D. Ark",
  "E.D. Cal", "E.D. Ky", "E.D. La", "E.D. Mich", "E.D. Mo", "E.D. Okla", "E.D. Pa", "E.D. Tenn", "E.D. Tex", "E.D. Va", "E.D. Wash", "E.D. Wis", "E.D.N.C", "E.D.N.Y", "ea", "Ecc", "Eccl", "Eccles", "Ecclus", "Ed", "Ed.D", "educ", "eld", "elev", "Eng", "engin", "Ens", "Eph", "Ephes", "Esd", "esq", "et al", "et seq", "et seqq", "etc", "eur", "ex", "Exd", "exec", "Exod", "exp", "ext", "Eze", "Ezek", "Ezr", "f", "F. Supp", "feat", "Feb", "Fed", "Fed. Cir", "Fed. Cl", "ff", "fi. fa", "fig", "fl", "fl. oz", "Fla", "flt", "frwy", "ft", "Ft", "fut", "G. E. A", "Ga", "Gal", "Gen", "Geo", "Gk", "Glag", "Goth", "Gov", 
"gram", "H.E", "Hab", "Hag", "Haw", "Heb", "Hebr", "Hos", "hr", "Hsa", "hwy", "i.a", "i.e", "i.p.i", "ib", "ibid", "Icel", "id", "il. col", "Ill", "imp", "impp", "in d", "inc", "Ind", "ind", "ins", "inst", "intl", "Isa", "It", "J.D", "Jam", "Jan", "Jas", "Jdg", "Jer", "Jhn", "Jno", "Jo", "Joe", "Jon", "Jos", "Josh", "Jud", "Judg", "Jul", "Jun", "K.K", "Kan", "Ki", "Ky", "L", "l", "l.c", "L.E.M", "La", "Lam", "Lament", "Lat", "leg", "Lev", "Lib", "lit", "Lit. Hum", "LL.M", "Loc", "Lt", "Ltd", "Luk", "M.D. Ala", "M.D. Fla", "M.D. Ga", "M.D. La", "M.D. Pa", "M.D. Tenn", "M.D.N.C", "m.m", "m.o", "Mac", "Macc",
 "Mal", "Man", "man", "Mar", "Mass", "Mat", "math", "Matt", "Me", "mech", "Messrs", "mfg", "mgmt", "mgt", "mi", "Mic", "mil", "min", "Minn", "Miss", "MissingNo", "mod.F", "Mons", "Mont", "Mpls", "Mr", "Ms", "MS", "Msgr", "mss", "MSS", "Mt", "mtg", "N. F", "n.a", "N.A", "N.B", "n.b", "N.C", "N.D", "n.d", "N.D. Ala", "N.D. Cal", "N.D. Fla", "N.D. Ga", "N.D. Ill", "N.D. Ind", "N.D. Miss", "N.D. Okla", "N.D. Tex", "N.D.N.Y", "N.D.W.Va", "n.g", "N.H", "n.h", "N.J", "N.M", "N.O.S", "n.t", "n.u", "Nah", "Nat", "natl", "Neb", "Nebr", "Neh", "nem. con", "Nev", "Newf", "no", "nol. pros", "nom", 
 "non obst", "Nov", "O.M", "Oba", "Obad", "obstetr", "obv", "Oct", "OF", "Off", "off", "OFris", "Okla", "Ont", "op", "opp", "Or", "Ore", "p", "P. I", "p.c", "P.I", "p.m", "P.O", "p.p", "p.pr", "P.R", "Pa", "Pe", "per cent", "perc", "pers", "Pet", "petrogr", "pf", "pg", "Pg", "PGmc", "Ph.D", "Phil", "Philem", "Philipp", "Phl", "pizz", "Pl", "pl", "poss", "pp", "Pr", "Pr. of Man",
  "prec", "pres", "Pro", "Prof", "Prov", "prox", "Ps", "Psa", "PSl", "Pss", "pt", "Pub", "Pub. L", "pwr", "q.t", "q.v", "quot", "r", "r.e", "R.I", "R.Ph", "rad", "rest", "Rev", "rev", "Rom", "Rt. Rev", "Rt. Revd", "Rth", "Ru", "Rus", "s", "S", "S. of III Ch", "S. of Sol", "S.A", "s.a", "S.Afr", "s.ap", "S.C", "S.D", "S.D. Ala", "S.D. Cal", "S.D. Fla", "S.D. Ga", "S.D. Ill", "S.D. Ind", "S.D. Miss",
 "S.D. Tex", "S.D.N.Y", "S.D.W.Va", "S.Dak", "s.l.a.n", "s.o", "S.O.S", "s.p", "s.p.s", "S.T", "s.t", "s.v", "Sab", "Sam", "Sar", "Sask", "Sat", "sbd", "sce", "Sec", "Sen", "sent", "Sep", "Sept", "sfz", "Sg", "sg", "sgd", "Sgs", "sing", "Skt", "So", "soc", "Song of Sol", "Song Sol", "sq", "ss", "Ste", "sub nom", "Sun", "Sup", "Sup. Ct", "Supp", "supt", "Sus", "symp", "T.O",
  "Tas", "Tenn", "Tex", "Th.D", "Theo", "Thess", "Thos", "Ti", "Tim", "Tit", "transf", "Tts", "U. S", "Ukr", "ult", "unk", "v", "V. I", "V.I", "v.s", "v.v", "Va", "var", "vb", "vb. n", "Vet", "Vet. App", "Vic", "vid", "Viet", "voc", "Voc", "vs", "Vt", "vv", "W", "W. O", "W.D", "W.D. Ark", "W.D. Ky", "W.D. La", "W.D. Mich", "W.D. Mo", "W.D. Okla", "W.D. Pa", "W.D. Tenn", "W.D. Tex", "W.D. Va", "W.D. Wash", "W.D. Wis", "W.D.N.C", "W.D.N.Y", "W.O", "W.Va", "Wash", "Wed", "Wis", "Wisd", "wit", "Wm", "Wyo", "x-div", "yd", "Zec", "Zech", "Zep", "Zeph", "zl"};*/

        return abbrevList;
    }


    
}
