DELIMITER $$

DROP PROCEDURE IF EXISTS `dyna2_dbo`.`updateIndex` $$
CREATE PROCEDURE `updateIndex`()
BEGIN

DECLARE done INT DEFAULT 0;

DECLARE idtoODT, indice int;
DECLARE valText nvarchar(100);
DECLARE inicio, inicioTmp int;
DECLARE valTextTmp nvarchar(100);

DECLARE curs3 CURSOR FOR
select id_to,max(val_texto) from o_datos_atrib
where id_to in(select id_to from s_index)
and property=2
group by id_to
order by id_to;



DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

OPEN curs3;
repeat
fetch curs3 into idtoODT, valText;
	IF isnumeric(valText)=1 then
		UPDATE S_INDEX SET `INDEX`=valText+1 WHERE ID_TO=idtoODT;
	ELSE
    			SET inicioTmp = LOCATE('-', valText, 0);
					IF inicioTmp<>0 /*caso guion*/ then
							WHILE (inicioTmp<>0) do
								SET inicio = inicioTmp;
								SET inicioTmp = LOCATE('-', valText, inicio+1);
							END while;
							SET valTextTmp = substring(valText, inicio+1, length(valText)-inicio);
							IF isnumeric(valTextTmp)=1 then
								UPDATE S_INDEX SET `INDEX`=valTextTmp+1 WHERE ID_TO=idtoODT;
              end if;
					ELSE
							SET inicioTmp = LOCATE('_', valText, 0);
							IF inicioTmp<>0 /*caso guion bajo*/ then
									WHILE (inicioTmp<>0) do
										SET inicio = inicioTmp;
										SET inicioTmp = LOCATE('_', valText, inicio+1);
    							END while;
									SET valTextTmp = substring(valText, inicio+1, length(valText)-inicio);
									IF isnumeric(valTextTmp)=1 then
										UPDATE S_INDEX SET `INDEX`=valTextTmp+1 WHERE ID_TO=idtoODT;
                  end if;
							END if;
				END if;
  end if;
until done END REPEAT;
CLOSE curs3;




END $$

DELIMITER ;